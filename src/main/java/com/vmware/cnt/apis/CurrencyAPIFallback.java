package com.vmware.cnt.apis;

import com.vmware.cnt.models.Conversion;
import com.vmware.cnt.services.TripsBudgetCalculator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

@Component
public class CurrencyAPIFallback implements CurrencyAPI{
    @Override
    public Mono<Conversion> getRate(String base, String target) {
        final Conversion.ConversionBuilder builder = Conversion.builder();
        builder.base(base);
        builder.date(LocalDate.now());
        builder.rates(Map.of(base, TripsBudgetCalculator.DEFAULT_RATE));
        return Mono.just(builder.build());
    }
}
