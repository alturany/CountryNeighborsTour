package com.vmware.cnt.apis;

import com.vmware.cnt.models.Conversion;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(value = "exchange-rates-api", url = "https://api.exchangeratesapi.io", fallback = CurrencyAPIFallback.class)
public interface CurrencyAPI {

    @GetMapping("/latest")
    Mono<Conversion> getRate(@RequestParam("base") String base, @RequestParam("symbols") String target);
}
