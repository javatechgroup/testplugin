package com.example.demo.controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;

@Service
@Configuration
public class ViolatingHttpClientTimeoutService {

    // VIOLATION 1: Direct new RestTemplate() in @Service
    public void callDownstreamService() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject("https://api.example.com/data", String.class);
    }

    // VIOLATION 2: HttpClient.newHttpClient() without connect timeout
    public void callWithJavaHttpClient() {
        HttpClient client = HttpClient.newHttpClient();
    }

    // VIOLATION 3: @Bean returning raw unconfigured RestTemplate
    @Bean
    public RestTemplate unconfiguredRestTemplateBean() {
        return new RestTemplate();
    }
}
