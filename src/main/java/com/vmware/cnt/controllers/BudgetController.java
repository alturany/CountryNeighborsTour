package com.vmware.cnt.controllers;

import com.vmware.cnt.dtos.TripsBudgetDTO;
import com.vmware.cnt.services.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class BudgetController {
    @Autowired
    private CountryService countryService;
    private final Set<String> countryCodes;
    private final Set<String> currencies;

    public BudgetController() {
        countryCodes = new HashSet<>(Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2));
        countryCodes.addAll(Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3));

        currencies = Currency.getAvailableCurrencies().stream().map(Currency::toString).collect(Collectors.toUnmodifiableSet());
    }

    @GetMapping("/budget")
    public TripsBudgetDTO calculateBudget(@RequestParam String startingCountryCode,
                                          @RequestParam @Positive int perVisitBudget,
                                          @RequestParam @Positive int totalBudget,
                                          @RequestParam String currency) {
        validateCountryCode(startingCountryCode);
        validateCurrency(currency);

        return countryService.calculateBudget(startingCountryCode, perVisitBudget, totalBudget, currency);
    }

    private void validateCountryCode(String countryCode) {
        final String countryCodeUpper = countryCode.toUpperCase();
        if (!countryCodes.contains(countryCodeUpper)) {
            throw new NoSuchElementException("Invalid countryCode parameter value");
        }
    }

    private void validateCurrency(String currency) {
        if (currency != null) {
            if (!currencies.contains(currency.toUpperCase())) {
                throw new NoSuchElementException("Invalid currency parameter value");
            }
        }
    }

}
