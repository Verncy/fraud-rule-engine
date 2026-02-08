package com.example.FraudRuleEngine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestClient pagerDutyRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://events.pagerduty.com")
                .build();
    }
}
