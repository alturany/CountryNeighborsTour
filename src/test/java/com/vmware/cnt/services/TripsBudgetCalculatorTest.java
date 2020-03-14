package com.vmware.cnt.services;

import com.vmware.cnt.dtos.DestinationDTO;
import com.vmware.cnt.dtos.TripsBudgetDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.Set;

import static com.vmware.cnt.services.CountryServiceTest.*;
import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
public class TripsBudgetCalculatorTest {

    @Mock
    private CountryService countryService;

    @Test
    public void givenTheWorldHasChangedAndBulgariaIsHaving2Neighbors_whenCallTripBudget_thenCalculateTheBudget() {
        //Given
        Mockito.doReturn(Mono.just(BULGARIA)).when(countryService).getCountry(BG);
        Mockito.doReturn(Mono.just(TURKEY)).when(countryService).getCountry(TR);
        Mockito.doReturn(Mono.just(GREECE)).when(countryService).getCountry(GR);
        Mockito.doReturn(Mono.just(0.51)).when(countryService).getConversionRate(BULGARIAN_CURRENCY, GREECE_CURRENCY);
        Mockito.doReturn(Mono.just(3.58)).when(countryService).getConversionRate(BULGARIAN_CURRENCY, TURKEY_CURRENCY);

        final TripsBudgetCalculator tripsBudgetCalculator = TripsBudgetCalculator.builder()
                .countryService(countryService)
                .homeCountryCurrency(BULGARIAN_CURRENCY)
                .totalBudget(500)
                .perCountryBudget(200)
                .homeCode(BG)
                .build();

        //When
        final TripsBudgetDTO result = tripsBudgetCalculator.invoke();

        //Then
        assertEquals(BULGARIAN_CURRENCY, result.getCurrency());
        assertEquals(100, result.getLeftover());
        assertEquals(1, result.getPerDestinationVisits());

        assertEquals(
                Set.of(
                        DestinationDTO.builder()
                                .country(TR)
                                .cost(716.0d)
                                .currency(TURKEY_CURRENCY)
                                .build(),
                        DestinationDTO.builder()
                                .country(GR)
                                .cost(102.0d)
                                .currency(GREECE_CURRENCY)
                                .build()
                ), result.getDestinations());
    }

    @Test
    public void givenYourCountryBudgetIsMoreThanOverAllBudget_whenCallTripBudget_thenZeroTrips(){
        //Given
        Mockito.doReturn(Mono.just(BULGARIA)).when(countryService).getCountry(BG);

        final TripsBudgetCalculator tripsBudgetCalculator = TripsBudgetCalculator.builder()
                .countryService(countryService)
                .homeCountryCurrency(BULGARIAN_CURRENCY)
                .totalBudget(100)
                .perCountryBudget(200)
                .homeCode(BG)
                .build();

        //When
        final TripsBudgetDTO result = tripsBudgetCalculator.invoke();

        //Then
        assertEquals(BULGARIAN_CURRENCY, result.getCurrency());
        assertEquals(100, result.getLeftover());
        assertEquals(0, result.getPerDestinationVisits());
        assertTrue(result.getDestinations().isEmpty());
    }

    @Test
    public void givenBudgetIsNotEnoughToVisitAllCountries_whenCallTripBudget_then(){
        //Given
        Mockito.doReturn(Mono.just(BULGARIA)).when(countryService).getCountry(BG);

        final TripsBudgetCalculator tripsBudgetCalculator = TripsBudgetCalculator.builder()
                .countryService(countryService)
                .homeCountryCurrency(BULGARIAN_CURRENCY)
                .totalBudget(100)
                .perCountryBudget(60)
                .homeCode(BG)
                .build();

        //When
        final TripsBudgetDTO result = tripsBudgetCalculator.invoke();

        //Then
        assertEquals(BULGARIAN_CURRENCY, result.getCurrency());
        assertEquals(100, result.getLeftover());
        assertEquals(0, result.getPerDestinationVisits());
        assertTrue(result.getDestinations().isEmpty());
    }
}
