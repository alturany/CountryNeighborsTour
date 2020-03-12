package com.vmware.cnt.apis;

import com.vmware.cnt.models.Country;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class CountryAPIFallback implements CountryAPI {
    @Override
    public Mono<Country> getCountry(String code) {
        final Country.CountryBuilder builder = Country.builder();
        builder.currencies(List.of("EUR"));
        builder.alpha2Code(code);
        builder.borders(List.of());
        return Mono.just(builder.build());
    }
}
