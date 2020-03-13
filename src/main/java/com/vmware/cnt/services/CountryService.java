package com.vmware.cnt.services;

import com.vmware.cnt.apis.CountryAPI;
import com.vmware.cnt.apis.CountryAPIFallback;
import com.vmware.cnt.apis.CurrencyAPI;
import com.vmware.cnt.dtos.TripsBudgetDTO;
import com.vmware.cnt.models.Country;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.function.Supplier;

@Service
@Slf4j
public class CountryService {
    public static final String COUNTRIES_CACHE = "countries";
    public static final String CONVERSIONS_CACHE = "exchange-rates";

    @Autowired
    private CountryAPI countryAPI;

    @Autowired
    private CurrencyAPI currencyAPI;

    @Autowired
    private CountryAPIFallback countryAPIFallback;

    @Autowired
    private CacheManager cacheManager;

    private Retry retry = Retry.ofDefaults("defaultRetry");

    public Mono<Country> getCountry(String countryCode) {
        log.debug("CountryService getCountry({})", countryCode);

        final Cache countriesCache = cacheManager.getCache(COUNTRIES_CACHE);

        return CacheMono
                .lookup(
                        k -> Mono.justOrEmpty(countriesCache.get(k, Country.class))
                                .map(Signal::next), countryCode)
                .onCacheMissResume(() -> {
                    final Supplier<Mono<Country>> countrySupplier = Retry.decorateSupplier(retry, () -> countryAPI.getCountry(countryCode));
                    final Try<Mono<Country>> result = Try.ofSupplier(countrySupplier)
                            .recover((throwable) -> countryAPIFallback.getCountry(countryCode));
                    return result.get();
                })
                .andWriteWith(
                        (k, sig) -> Mono.fromRunnable(() -> {
                            if (!sig.hasError()) {
                                log.debug("getCountry K: {} Signal: {}", k, sig.get());
                                countriesCache.putIfAbsent(k, sig.get());
                            }
                        })
                )
                .doOnError((error) -> {
                    log.error("The following error happened on getCountry({}) method!", countryCode);
                    throw new RuntimeException(error);
                });
    }


    public Mono<Double> getConversionRate(String base, String toCurrency) {
        log.debug("getConversionRate({},{})", base, toCurrency);
        final Cache conversionsCache = cacheManager.getCache(CONVERSIONS_CACHE);
        final String key = base + "-" + toCurrency;

        return CacheMono.lookup(
                k -> Mono.justOrEmpty(conversionsCache.get(k, Double.class))
                        .map(Signal::next), key)
                .onCacheMissResume(
                        () -> currencyAPI.getRate(base, toCurrency)
                                .flatMapIterable(conversion -> conversion.getRates().values())
                                .elementAt(0))
                .andWriteWith(
                        (k, sig) -> Mono.fromRunnable(() -> {
                            if (!sig.hasError()) {
                                log.debug("conversions K: {} Signal: {}", k, sig.get());
                                conversionsCache.putIfAbsent(k, sig.get());
                            }
                        }))
                .doOnError(error -> {
                    log.error("The following error happened on getConversionRate({},{}) method!", base, toCurrency);
                    throw new RuntimeException(error);
                });
    }

    @Cacheable(value="budgets", key = "#homeCode.concat('-').concat(#perCountryBudget).concat('-').concat(#totalBudget).concat(#currency)")
    public TripsBudgetDTO calculateBudget(String homeCode, int perCountryBudget, int totalBudget, String currency) {
        return TripsBudgetCalculator.builder()
                .homeCode(homeCode)
                .perCountryBudget(perCountryBudget)
                .totalBudget(totalBudget)
                .homeCountryCurrency(currency)
                .countryService(this)
                .build()
                .invoke();
    }

}
