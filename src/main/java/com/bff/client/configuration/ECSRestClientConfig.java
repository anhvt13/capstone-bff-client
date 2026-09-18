package com.bff.client.configuration;

import com.bff.client.controller.BffController;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

import java.net.InetAddress;

//TODO: "ecs" profile to leverage discovers/routes to ECS service managed by AWS
@Configuration
@Profile("ecs")
public class ECSRestClientConfig {

    private static final Logger log = LoggerFactory.getLogger(ECSRestClientConfig.class);

    @Bean
    public RestClient.Builder driverRestClientBuilder(RestClientFactory factory, OAuth2AuthorizedClientManager authorizedClientManager) throws Exception {
        return factory.createSecuredRestClient(authorizedClientManager);
    }

    @PostConstruct
    public void init() {
        log.info(">>> USING ECS REST CLIENT CONFIG");
        try {
            InetAddress[] addresses = InetAddress.getAllByName("driver-service");
            for (InetAddress address : addresses) {
                log.info(">>> driver-service resolves to {}", address.getHostAddress());
            }
        } catch (Exception e) {
            log.error(">>> FAILED TO RESOLVE driver-service", e
            );
        }
    }

}
