package com.example.FraudRuleEngine.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertSettings {

    // Supported: "WEBHOOK", "PAGERDUTY", "SLACK"
    public String provider() {
        return getEnv("ALERT_PROVIDER", "WEBHOOK").toUpperCase();
    }


    public boolean enabled() {
        return Boolean.parseBoolean(getEnv("ALERT_ENABLED", "true"));
    }

    // For WEBHOOK and SLACK (Slack incoming webhook URL)
    public String webhookUrl() {
        return getEnv("ALERT_WEBHOOK_URL", "");
    }

    // For PagerDuty (keep for later when you have access)
    public String pagerDutyRoutingKey() {
        return getEnv("PAGERDUTY_ROUTING_KEY", "");
    }

    public String source() {
        return getEnv("ALERT_SOURCE", "fraud-rule-engine");
    }

    // Generic severities
    public String severityHigh() {
        return getEnv("ALERT_SEVERITY_HIGH", "critical");
    }

    private String getEnv(String key, String defaultVal) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? defaultVal : v;
    }
}
