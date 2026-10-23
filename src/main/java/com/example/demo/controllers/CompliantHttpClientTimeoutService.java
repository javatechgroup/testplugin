package com.example.demo.controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

@Service
@Configuration
public class CompliantHttpClientTimeoutService {

    private final RestTemplate restTemplate;

    // COMPLIANT: Injecting configured RestTemplate bean
    public CompliantHttpClientTimeoutService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void callSafeDownstream() {
        // COMPLIANT: Standard HttpClient configured with explicit connect timeout
        HttpClient safeClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    // COMPLIANT: @Bean configuring request factory with timeouts
    @Bean
    public RestTemplate manualConfiguredRestTemplateBean() {
        RestTemplate rt = new RestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        rt.setRequestFactory(factory);
        return rt;
    }
}
