package com.vmware.cnt.services;

import com.vmware.cnt.apis.CountryAPI;
import com.vmware.cnt.apis.CountryAPIFallback;
import com.vmware.cnt.apis.CurrencyAPI;
import com.vmware.cnt.models.Conversion;
import com.vmware.cnt.models.Country;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.vmware.cnt.services.CountryService.CONVERSIONS_CACHE;
import static com.vmware.cnt.services.CountryService.COUNTRIES_CACHE;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CountryService.class)
public class CountryServiceTest {


    @Mock
    private CountryAPI countryAPI;

    @MockBean
    private CurrencyAPI currencyAPI;

    @MockBean
    private CountryAPIFallback countryAPIFallback;

    @MockBean
    private CacheManager cacheManager;

    @Autowired
    private CountryService countryService;


    public final static String BG = "BG";
    public final static String BULGARIAN_CURRENCY = "BGN";
    public static final String GR = "GR";
    public static final String GREECE_CURRENCY = "EUR";
    public static final String TR = "TR";
    public static final String TURKEY_CURRENCY = "TRY";

    public final static Country BULGARIA = Country.builder()
            .alpha2Code(BG)
            .currency(BULGARIAN_CURRENCY)
            .name("Bulgaria")
            .border(GR)
            .border(TR)
            .build();

    public final static Country GREECE = Country.builder()
            .alpha2Code(GR)
            .currency(GREECE_CURRENCY)
            .name("GREECE")
            .build();

    public final static Country TURKEY = Country.builder()
            .alpha2Code(TR)
            .currency(TURKEY_CURRENCY)
            .name("Turkey")
            .build();

    final Country de = Country.builder()
            .alpha2Code("DE")
            .build();

    final Cache countriesCache = Mockito.mock(Cache.class);
    final Cache conversionsCache = Mockito.mock(Cache.class);


    @Before
    public void setup() {
        Mockito.doReturn(countriesCache).when(cacheManager).getCache(COUNTRIES_CACHE);
        Mockito.doReturn(conversionsCache).when(cacheManager).getCache(CONVERSIONS_CACHE);
    }

    @Test
    public void givenCacheDoesNotContainCountryAndAPIIsFailing_whenGetCountry_thenCallFallbakMethod() {
        // Given
        Mockito.doReturn(null).when(countriesCache).get(BG, Country.class);
        Mockito.doThrow(new RuntimeException()).when(countryAPI).getCountry(BG);
        Mockito.doReturn(Mono.just(de)).when(countryAPIFallback).getCountry(BG);
        // When
        final Mono<Country> result = countryService.getCountry(BG);

        // Then
        StepVerifier.create(result)
                .expectNext(de)
                .then(() -> verify(countryAPIFallback).getCountry(BG))
                .verifyComplete();
    }


    @Test
    public void givenCacheContainsCurrency_whenGetCurrency_thenCacheHit() {
        //Given
        final String base = "EUR";
        final String toCurrency = BULGARIAN_CURRENCY;
        final Double rate = 1.96d;

        Mockito.doReturn(rate).when(conversionsCache).get(base + "-" + toCurrency, Double.class);
        Mockito.doReturn(Mono.just(Conversion.builder().rate(BULGARIAN_CURRENCY, 2.5).build()))
                .when(currencyAPI).getRate(base, toCurrency);

        // When
        final Mono<Double> result = countryService.getConversionRate(base, toCurrency);

        // Then
        StepVerifier.create(result)
                .expectNext(rate)
                .expectComplete()
                .verify();
    }

}
