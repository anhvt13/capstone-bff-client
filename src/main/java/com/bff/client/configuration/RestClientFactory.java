package com.bff.client.configuration;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

@Component
public class RestClientFactory {

    @Value("${client.ssl.key-store-password}")
    private String keystorePassword;

    @Value("${server.ssl.trust-store-password}")
    private String truststorePassword;

    @Value("${client.ssl.key-alias}")
    private String keyAlias;

    @Value("${key-store.path}")
    private String keyStorePath;

    @Value("${truststore.path}")
    private String trustStorePath;

    private static final Logger log = LoggerFactory.getLogger(RestClientFactory.class);

    public RestClient.Builder createSecuredRestClient(OAuth2AuthorizedClientManager authorizedClientManager) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException, UnrecoverableKeyException {

        //TODO: LOAD TRUSTSTORE - This is the CA that signed driver-service's certificate.
        Resource trustStore = new FileSystemResource(trustStorePath);
        KeyStore trustKeyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = trustStore.getInputStream()) {
            trustKeyStore.load(in, truststorePassword.toCharArray());
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustKeyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        /* * Diagnostic: show exactly what Java trusts. */
        X509ExtendedTrustManager originalTrustManager = null;
        for (TrustManager trustManager : trustManagers) {
            log.info(">>> TrustManager = {}", trustManager.getClass().getName());
            if (trustManager instanceof X509ExtendedTrustManager x509TrustManager) {
                originalTrustManager =  x509TrustManager;
                log.info(">>> Accepted issuers = {}", x509TrustManager.getAcceptedIssuers().length);
                for (X509Certificate certificate : x509TrustManager.getAcceptedIssuers()) {
                    log.info(">>> TRUSTED CA = {}", certificate.getSubjectX500Principal());
                }
            }
        }

        //TODO: LOAD KEYSTORE - The driver-service requires a client certificate
        Resource keyStore = new FileSystemResource(keyStorePath);
        KeyStore clientKeyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = keyStore.getInputStream()) {
            clientKeyStore.load(in, keystorePassword.toCharArray());
        }
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(clientKeyStore, keystorePassword.toCharArray());

        //TODO: Configure TLS handshake connection
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);
        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder
                .create()
                .setSslContext(sslContext)
                .build();

        //TODO: Initialize Connection Pool
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .setMaxConnTotal(20)
                .setMaxConnPerRoute(5)
                .setSSLSocketFactory(sslSocketFactory)
                .setConnectionTimeToLive(TimeValue.ofMinutes(5))
                .setValidateAfterInactivity( TimeValue.ofSeconds(10))
                .build();

        //TODO: Setup connection time-out
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(2))
                .setConnectTimeout(Timeout.ofSeconds(2))
                .setResponseTimeout(Timeout.ofSeconds(5))
                .build();

        //TODO: HTTP Client
        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
//                .evictExpiredConnections()
//                .evictIdleConnections(TimeValue.ofSeconds(30))
                .build();

        //TODO: HTTP Request Factory
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        //TODO: OAUTH2 Interceptor
        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        interceptor.setClientRegistrationIdResolver(new RequestAttributeClientRegistrationIdResolver());

        return RestClient.builder()
                .requestFactory(requestFactory)
                .requestInterceptor(interceptor);
    }
}
