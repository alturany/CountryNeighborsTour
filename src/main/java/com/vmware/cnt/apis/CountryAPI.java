package com.vmware.cnt.apis;

import com.vmware.cnt.models.Country;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(qualifier = "CountryAPI", value = "rest-countries-v1", url = "https://restcountries-v1.p.rapidapi.com")
public interface CountryAPI {

    @GetMapping("/alpha/{code}" )
    Mono<Country> getCountry(@PathVariable String code);

}
