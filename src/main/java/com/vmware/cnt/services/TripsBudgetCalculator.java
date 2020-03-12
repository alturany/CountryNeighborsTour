package com.vmware.cnt.services;

import com.vmware.cnt.dtos.DestinationDTO;
import com.vmware.cnt.dtos.TripsBudgetDTO;
import com.vmware.cnt.models.Country;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.javamoney.moneta.FastMoney;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Builder
public class TripsBudgetCalculator {
    private int perCountryBudget;
    private int totalBudget;
    private String homeCountryCurrency;
    private String homeCode;
    private final TripsBudgetDTO.TripsBudgetDTOBuilder budgetDTOBuilder = TripsBudgetDTO.builder();
    private CountryService countryService;

    TripsBudgetDTO invoke() {
        final Long perDestinationVisits = calculateBudgetSummary();

        final Flux<ImmutableTriple<String, FastMoney, Double>> visitsCosts = calculateCountriesVisitsCosts(perDestinationVisits);
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

    private Flux<ImmutableTriple<String, FastMoney, Double>> calculateCountriesVisitsCosts(Long perDestinationVisits) {
        return getBorders().flatMap(country -> {
            final Mono<String> currency = getCurrency(country);
            return currency.map(cur -> ImmutablePair.of(country, cur));
        }).flatMap(pair -> {
            final String country = pair.left;
            final String currency = pair.right;
            final Mono<Double> conversionRate = countryService.getConversionRate(homeCountryCurrency, currency);

            return conversionRate.map(rate -> {
                double cost = perCountryBudget;
                String usedCurrency = homeCountryCurrency;
                if (rate != 1) {
                    cost = BigDecimal.valueOf(perCountryBudget)
                            .multiply(BigDecimal.valueOf(rate))
                            .setScale(3, RoundingMode.HALF_DOWN)
                            .doubleValue();
                    usedCurrency = currency;
                }
                log.debug("Cost of visit {}{} used rate: {}", cost, currency, rate);
                return ImmutableTriple.of(country, FastMoney.of(cost * perDestinationVisits, usedCurrency), rate);
            });
        }).doOnError(error -> {
            log.error("The following error happened on calculateCountriesVisitsCosts method!", error);
            throw new RuntimeException(error);
        });
    }


    private Mono<String> getCurrency(String countryCode) {
        //The assumption that I will use the first currency for multi currency countries
        return countryService.getCountry(countryCode).map((country) -> country.getCurrencies().get(0));
    }

    private void addTripsDetails(Flux<ImmutableTriple<String, FastMoney, Double>> visitsCosts) {
        final List<DestinationDTO> dList = visitsCosts.map((visitCost) -> {
            DestinationDTO destination = new DestinationDTO();
            final String country = visitCost.left;
            final FastMoney cost = visitCost.middle;
            destination.setCost(cost.getNumber().doubleValue());
            destination.setCountry(country);
            destination.setCurrency(cost.getCurrency().getCurrencyCode());
            return destination;
        }).collectList().block();
        budgetDTOBuilder.destinations(dList);
    }
}
