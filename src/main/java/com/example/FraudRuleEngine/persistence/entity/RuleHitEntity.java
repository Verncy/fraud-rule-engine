package com.example.FraudRuleEngine.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "rule_hits")
public class RuleHitEntity {

    @Id
    @Column(name = "hit_id", nullable = false)
    private UUID hitId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private FraudCaseEntity fraudCase;

    @Column(name = "rule_id", nullable = false, length = 64)
    private String ruleId;

    @Column(name = "rule_version", nullable = false, length = 16)
    private String ruleVersion;

    @Column(name = "severity", nullable = false, length = 16)
    private String severity;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (hitId == null) hitId = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public UUID getHitId() { return hitId; }

    public FraudCaseEntity getFraudCase() { return fraudCase; }
    public void setFraudCase(FraudCaseEntity fraudCase) { this.fraudCase = fraudCase; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleVersion() { return ruleVersion; }
    public void setRuleVersion(String ruleVersion) { this.ruleVersion = ruleVersion; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public JsonNode getMetadata() { return metadata; }
    public void setMetadata(JsonNode metadata) { this.metadata = metadata; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
