package com.example.FraudRuleEngine.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fraud_cases")
public class FraudCaseEntity {

    @Id
    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @OneToOne(optional = false)
    @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id", nullable = false, unique = true)
    private TransactionEntity transaction;

    @Column(name = "flagged", nullable = false)
    private boolean flagged;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @OneToMany(mappedBy = "fraudCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RuleHitEntity> ruleHits = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (caseId == null) caseId = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    // getters/setters

    public UUID getCaseId() { return caseId; }

    public TransactionEntity getTransaction() { return transaction; }
    public void setTransaction(TransactionEntity transaction) { this.transaction = transaction; }

    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public List<RuleHitEntity> getRuleHits() { return ruleHits; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
