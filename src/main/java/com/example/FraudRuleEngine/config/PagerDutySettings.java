package com.example.FraudRuleEngine.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class PagerDutySettings {

    // You can also hardcode defaults here if you want
    public boolean enabled() {
        return Boolean.parseBoolean(getEnv("PAGERDUTY_ENABLED", "true"));
    }

    public String routingKey() {
        return getEnv("PAGERDUTY_ROUTING_KEY", "");
    }

    public String source() {
        return getEnv("PAGERDUTY_SOURCE", "fraud-rule-engine");
    }

    public String defaultSeverity() {
        // PagerDuty supports: info, warning, error, critical
        return getEnv("PAGERDUTY_SEVERITY", "error");
    }

    private String getEnv(String key, String defaultVal) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? defaultVal : v;
    }
}
