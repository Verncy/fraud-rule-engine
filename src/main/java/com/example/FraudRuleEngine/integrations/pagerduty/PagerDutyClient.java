package com.example.FraudRuleEngine.integrations.pagerduty;

import com.example.FraudRuleEngine.config.PagerDutySettings;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class PagerDutyClient {

    private final RestClient restClient;
    private final PagerDutySettings settings;

    public PagerDutyClient(RestClient pagerDutyRestClient, PagerDutySettings settings) {
        this.restClient = pagerDutyRestClient;
        this.settings = settings;
    }

    public void triggerFlaggedTransaction(String transactionId, String summary, Map<String, Object> customDetails) {
        if (!settings.enabled()) return;
        if (settings.routingKey().isBlank()) return;

        Map<String, Object> body = Map.of(
                "routing_key", settings.routingKey(),
                "event_action", "trigger",
                "dedup_key", transactionId,
                "payload", Map.of(
                        "summary", summary,
                        "source", settings.source(),
                        "severity", settings.defaultSeverity(),
                        "custom_details", customDetails
                )
        );

        restClient.post()
                .uri("/v2/enqueue")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
