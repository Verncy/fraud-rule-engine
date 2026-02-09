package com.example.FraudRuleEngine.service.events;

import com.example.FraudRuleEngine.integrations.MonitoringClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.HashMap;
import java.util.Map;

@Component
public class FraudFlaggedListener {

    private static final Logger log = LoggerFactory.getLogger(FraudFlaggedListener.class);

    private final MonitoringClient monitoringClient;

    public FraudFlaggedListener(MonitoringClient monitoringClient) {
        this.monitoringClient = monitoringClient;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFraudFlagged(FraudFlaggedEvent e) {
        MDC.put("transactionId", e.transactionId());
        try {
            log.info("FraudFlaggedListener fired riskScore={} anyHigh={} merchant={} amount={} {}",
                    e.riskScore(), e.anyHigh(), e.merchant(), e.amount(), e.currency());

            String summary = "FLAGGED fraud case: tx=" + e.transactionId()
                    + " riskScore=" + e.riskScore()
                    + " merchant=" + e.merchant()
                    + " amount=" + e.amount() + " " + e.currency()
                    + " anyHigh=" + e.anyHigh();

            Map<String, Object> details = new HashMap<>();
            details.put("transactionId", e.transactionId());
            details.put("riskScore", e.riskScore());
            details.put("anyHigh", e.anyHigh());
            details.put("customerId", e.customerId());
            details.put("merchant", e.merchant());
            details.put("amount", e.amount());
            details.put("currency", e.currency());
            details.put("eventTime", e.eventTime());
            details.put("ruleIds", e.ruleIds());

            log.info("Sending alert via MonitoringClient...");
            monitoringClient.notifyHighRisk(e.transactionId(), summary, details);
            log.info("MonitoringClient call completed.");
        } finally {
            MDC.remove("transactionId");
        }
    }
}
