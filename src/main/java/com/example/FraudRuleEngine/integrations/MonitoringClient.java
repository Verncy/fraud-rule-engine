package com.example.FraudRuleEngine.integrations;

import com.example.FraudRuleEngine.config.AlertSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Simple monitoring integration.
 *
 * For the take-home submission we support:
 *  - WEBHOOK (recommended demo path; works with webhook.site)
 *  - SLACK (incoming webhook)
 *  - PAGERDUTY (kept for future; requires routing key)
 */
@Component
public class MonitoringClient {

    private static final Logger log = LoggerFactory.getLogger(MonitoringClient.class);

    private final RestClient restClient;
    private final AlertSettings settings;

    public MonitoringClient(RestClient restClient, AlertSettings settings) {
        this.restClient = restClient;
        this.settings = settings;
    }

    public void notifyHighRisk(String transactionId, String summary, Map<String, Object> details) {
        log.info("MonitoringClient.notifyHighRisk provider={} enabled={}", settings.provider(), settings.enabled());

        if (!settings.enabled()) {
            return;
        }

        switch (settings.provider()) {
            case "WEBHOOK" -> sendWebhook(summary, details);
            case "SLACK" -> sendSlack(summary, details);
            case "PAGERDUTY" -> sendPagerDuty(transactionId, summary, details);
            default -> log.warn("Unknown alert provider '{}'; no alert sent", settings.provider());
        }
    }

    private void sendWebhook(String summary, Map<String, Object> details) {
        String url = settings.webhookUrl();

        if (url == null || url.isBlank()) {
            url = "https://webhook.site/be3ed73b-430c-4212-b08d-da58503217a3";
            log.warn("ALERT_WEBHOOK_URL is blank; using fallback demo webhook: {}", url);
        }

        log.info("Posting WEBHOOK alert to url={}", url);

        Map<String, Object> body = Map.of(
            "source", settings.source(),
            "severity", settings.severityHigh(),
            "summary", summary,
            "details", details
        );

        try {
            var resp = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Webhook POST done status={}", resp.getStatusCode());
        } catch (Exception ex) {
            log.error("Webhook POST failed: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        }
    }

    private void sendSlack(String summary, Map<String, Object> details) {
        String url = settings.webhookUrl();
        if (url == null || url.isBlank()) {
            log.warn("ALERT_WEBHOOK_URL is blank; Slack alert not sent");
            return;
        }

        String text = "🚨 *HIGH Fraud Alert*\n" + summary + "\n```" + details + "```";
        Map<String, Object> body = Map.of("text", text);

        restClient.post()
                .uri(settings.webhookUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        log.info("Slack webhook alert sent");
    }

    private void sendPagerDuty(String transactionId, String summary, Map<String, Object> details) {
        // Kept for completeness; requires a PagerDuty Events API v2 routing key.
        if (settings.pagerDutyRoutingKey().isBlank()) {
            log.warn("PAGERDUTY_ROUTING_KEY is blank; PagerDuty alert not sent");
            return;
        }

        Map<String, Object> body = Map.of(
                "routing_key", settings.pagerDutyRoutingKey(),
                "event_action", "trigger",
                "dedup_key", transactionId,
                "payload", Map.of(
                        "summary", summary,
                        "source", settings.source(),
                        "severity", settings.severityHigh(),
                        "custom_details", details
                )
        );

        restClient.post()
                .uri("https://events.pagerduty.com/v2/enqueue")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        log.info("PagerDuty event enqueued (dedup_key={})", transactionId);
    }
}
