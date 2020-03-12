package com.vmware.cnt.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import reactivefeign.client.ReactiveHttpRequest;
import reactivefeign.client.ReactiveHttpRequestInterceptor;

import java.util.List;

@Configuration
public class RapidApiHeadersInterceptor implements ReactiveHttpRequestInterceptor {
    @Value("${rapid.api.key}")
    private String rapidAPIKey;

    @Override
    public ReactiveHttpRequest apply(ReactiveHttpRequest reactiveHttpRequest) {
        reactiveHttpRequest.headers().put("x-rapidapi-key", List.of(rapidAPIKey));
        reactiveHttpRequest.headers().put("x-rapidapi-host", List.of("restcountries-v1.p.rapidapi.com"));
        return reactiveHttpRequest;
    }

}