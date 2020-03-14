# CountryNeighborsTour

Maven based Spring boot 2 API which has been built using:
1) Reactive Feign Client
2) Ehcache
3) Lombok
4) Resilience4j-Retry: Used it to retry country API calls, and depended on the default retry configs
5) Reactor
6) Reactor Extra utils: Spring built-in caching `@Cacheable & ... other annotations` does not support caching of `Mono` & `Flux` so used this functional lib to cache Feign call results
7) Spring Security & OAuth: to implement the 2 bonuses 
8) Swagger using springdoc-openapi-ui


To run the program:
1) value of `google.client-id` in `application.properties` has to be changed based on the value you get from Google APIs Console
2) `google.client.secret` env variable has to be passed to the java application
3) `rapid.api.key` env variable has to be passed too, this is the key you get after registering in rapidAPI

Secrets `google.client.secret` & `rapid.api.key` should be stored in secure place and for simple application an env variable could be a good fit, but for production applications it would be better to store them in secrets manager like Hashicorp Vault 
