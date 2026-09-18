package com.bff.client.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

//TODO: Default profile to leverage Eureka service discovery/routes managed by Spring Cloud
@Configuration
@Profile("local")
public class SpringRestClientConfig {

    private static final Logger log = LoggerFactory.getLogger(SpringRestClientConfig.class);

    //TODO: Initialize non-balancer client for Eureka internal to query service discovery
    // Reuse load-balance client would trigger a lookup and recurse indefinitely.
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        log.info(">>> CREATING DEFAULT SPRING REST CLIENT CONFIG");
        return RestClient.builder();
    }

    //TODO: Intending a @LoadBalancer rest client to leverage Eureka service-discovery & Spring load-balancer mechanism
    @Bean
    @LoadBalanced
    public RestClient.Builder driverRestClientBuilder(RestClientFactory factory, OAuth2AuthorizedClientManager authorizedClientManager) throws Exception {
        log.info(">>> CREATING SPRING LOAD-BALANCED REST CLIENT BUILDER");
        return factory.createSecuredRestClient(authorizedClientManager);
    }
}
