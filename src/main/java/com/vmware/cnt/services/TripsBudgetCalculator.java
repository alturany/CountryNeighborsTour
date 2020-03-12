package com.vmware.cnt.services;

import com.vmware.cnt.dtos.DestinationDTO;
import com.vmware.cnt.dtos.TripsBudgetDTO;
import com.vmware.cnt.models.Country;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.javamoney.moneta.FastMoney;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Builder
public class TripsBudgetCalculator {
    public static final double DEFAULT_RATE = 1.0;
    private int perCountryBudget;
    private int totalBudget;
    private String homeCountryCurrency;
    private String homeCode;
    private final TripsBudgetDTO.TripsBudgetDTOBuilder budgetDTOBuilder = TripsBudgetDTO.builder();
    private CountryService countryService;

    TripsBudgetDTO invoke() {
        final Long perDestinationVisits = calculateBudgetSummary();

        final Flux<ImmutablePair<String, FastMoney>> visitsCosts = calculateCountriesVisitsCosts(perDestinationVisits);
        if (perDestinationVisits != 0) {
            addTripsDetails(visitsCosts);
        }
        return budgetDTOBuilder.build();

    }

    private Long calculateBudgetSummary() {
        final Mono<Long> bordersCount = getBorders().count();
        final Mono<Long> leftover = bordersCount.map(count -> totalBudget % (perCountryBudget * count));
        final Long perDestinationVisits = leftover.map(l -> totalBudget - l).zipWith(bordersCount, (usedBudget, bc) -> (usedBudget / (bc * perCountryBudget))).block();
        budgetDTOBuilder.destinationVisits(perDestinationVisits);
        budgetDTOBuilder.currency(homeCountryCurrency);
        budgetDTOBuilder.leftover(leftover.block());
        return perDestinationVisits;
    }

    private Flux<String> getBorders() {
        return countryService.getCountry(homeCode).flatMapIterable(Country::getBorders).cache();
    }

    private Flux<ImmutablePair<String, FastMoney>> calculateCountriesVisitsCosts(Long perDestinationVisits) {
        return getBorders().flatMap(country -> {
            final Mono<String> currency = getSingleCurrency(country);
            return currency.map(cur -> ImmutablePair.of(country, cur));
        }).flatMap(pair -> {
            final String country = pair.left;
            final String currency = pair.right;
            final Mono<Double> conversionRate = getConversionRate(country);

            return conversionRate.map(rate -> {
                double cost = perCountryBudget;
                String usedCurrency = homeCountryCurrency;
                if (rate != DEFAULT_RATE) {
                    cost = BigDecimal.valueOf(perCountryBudget)
                            .multiply(BigDecimal.valueOf(rate))
                            .setScale(3, RoundingMode.HALF_DOWN)
                            .doubleValue();
                    usedCurrency = currency;
                }
                log.debug("Cost of visit {}{} used rate: {}", cost, currency, rate);
                return ImmutablePair.of(country, FastMoney.of(cost * perDestinationVisits, usedCurrency));
            });
        }).doOnError(error -> {
            log.error("The following error happened on calculateCountriesVisitsCosts method!", error);
            throw new RuntimeException(error);
        });
    }

    private Mono<Double> getConversionRate(String countryCode) {
        // for multi currency countries the API might not support all currency names so I had to call the API for all
        // i.e (name=Switzerland currencies=[CHE, CHF, CHW] the API only knows CHF
        return countryService.getCountry(countryCode)
                .flatMapIterable(Country::getCurrencies)
                .flatMap(currency -> countryService.getConversionRate(homeCountryCurrency, currency))
                .filter(rate -> rate != DEFAULT_RATE)
                .defaultIfEmpty(DEFAULT_RATE)
                .elementAt(0);
    }


    private Mono<String> getSingleCurrency(String countryCode) {
        //I will use the first currency for multi currency countries
        return countryService.getCountry(countryCode).map((country) -> country.getCurrencies().get(0));
    }


    private void addTripsDetails(Flux<ImmutablePair<String, FastMoney>> visitsCosts) {
        final List<DestinationDTO> dList = visitsCosts.map((visitCost) -> {
            DestinationDTO destination = new DestinationDTO();
            final String country = visitCost.left;
            final FastMoney cost = visitCost.right;
            destination.setCost(cost.getNumber().doubleValue());
            destination.setCountry(country);
            destination.setCurrency(cost.getCurrency().getCurrencyCode());
            return destination;
        }).collectList().block();
        budgetDTOBuilder.destinations(dList);
    }
}
