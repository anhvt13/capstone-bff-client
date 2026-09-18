package com.bff.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

@Component
public class EurekaDebugRunner implements CommandLineRunner {

    private final DiscoveryClient discoveryClient;

    public EurekaDebugRunner(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public void run(String... args) {
        var instances = discoveryClient.getInstances("driver-service");

        instances.forEach(instance -> {
            System.out.println("===== DRIVER INSTANCE =====");
            System.out.println("instanceId = " + instance.getInstanceId());
            System.out.println("host       = " + instance.getHost());
            System.out.println("port       = " + instance.getPort());
            System.out.println("secure     = " + instance.isSecure());
            System.out.println("URI        = " + instance.getUri());
        });
    }
}