package com.bff.client.service;

import com.bff.client.configuration.RestClientFactory;
import com.bff.client.model.Driver;
import com.bff.client.model.SliceDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Service
public class DriverService {

    //TODO: Loaded OAuth2 Client Registration Id in application properties
    public static final String DRIVER_SERVICE_CLIENT = "driver-service";

    @Value("${driver.service.base-url}")
    public String DRIVER_SERVICE_BASE_URL;

    @Autowired
    @Qualifier("driverRestClientBuilder")
    private RestClient.Builder driverRestClientBuilder;

    private static final Logger log = LoggerFactory.getLogger(DriverService.class);

    public SliceDTO<Driver> getDrivers(MultiValueMap<String, String> queryParams) {
        log.info(">>> DRIVER CLIENT USING BUILDER: {}", System.identityHashCode(driverRestClientBuilder));

        String uriString = new StringBuilder()
                .append(DRIVER_SERVICE_BASE_URL)
                .append("/drivers")
                .toString();

        URI uri = UriComponentsBuilder
                .fromUriString(uriString)
                .queryParams(queryParams)
                .build()
                .toUri();

        SliceDTO<Driver> driverSlice = driverRestClientBuilder.build()
                .get()
                .uri(uri)
                .attributes(clientRegistrationId(DRIVER_SERVICE_CLIENT))
                .retrieve()
                .body(SliceDTO.class);

        return driverSlice;
    }

    public Driver getDriver(Integer driverId) {
        String uriString = new StringBuilder()
                .append(DRIVER_SERVICE_BASE_URL)
                .append("/driver/")
                .append(driverId)
                .toString();

        URI uri = UriComponentsBuilder
                .fromUriString(uriString)
                .build()
                .toUri();

        return driverRestClientBuilder.build().get()
                .uri(uri)
                .attributes(clientRegistrationId(DRIVER_SERVICE_CLIENT))
                .retrieve()
                .body(Driver.class);
    }

    public Driver saveDriver(@Valid Driver driver) {
        String uriString = new StringBuilder()
                .append(DRIVER_SERVICE_BASE_URL)
                .append("/driver")
                .toString();

        URI uri = UriComponentsBuilder
                .fromUriString(uriString)
                .build()
                .toUri();

        return driverRestClientBuilder.build().post()
                .uri(uri)
                .body(driver)
                .attributes(clientRegistrationId(DRIVER_SERVICE_CLIENT))
                .retrieve()
                .body(Driver.class);
    }

    public void deleteDriver(Integer driverId) {
        String uriString = new StringBuilder()
                .append(DRIVER_SERVICE_BASE_URL)
                .append("/driver/")
                .append(driverId)
                .toString();

        URI uri = UriComponentsBuilder
                .fromUriString(uriString)
                .build()
                .toUri();

        driverRestClientBuilder.build().delete()
                .uri(uri)
                .attributes(clientRegistrationId(DRIVER_SERVICE_CLIENT))
                .retrieve()
                .toBodilessEntity();
    }
}
