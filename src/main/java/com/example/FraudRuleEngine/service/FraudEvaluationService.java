package com.example.FraudRuleEngine.service;

import com.example.FraudRuleEngine.api.dto.EvaluateResponse;
import com.example.FraudRuleEngine.api.dto.RuleHitDto;
import com.example.FraudRuleEngine.api.dto.TransactionEventRequest;
import com.example.FraudRuleEngine.domain.model.TransactionEvent;
import com.example.FraudRuleEngine.domain.rules.FraudRule;
import com.example.FraudRuleEngine.domain.rules.RuleHit;
import com.example.FraudRuleEngine.domain.rules.Severity;
import com.example.FraudRuleEngine.persistence.entity.FraudCaseEntity;
import com.example.FraudRuleEngine.persistence.entity.RuleHitEntity;
import com.example.FraudRuleEngine.persistence.entity.TransactionEntity;
import com.example.FraudRuleEngine.persistence.repo.FraudCaseRepository;
import com.example.FraudRuleEngine.persistence.repo.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FraudEvaluationService {

    private final List<FraudRule> rules;
    private final TransactionRepository transactionRepository;
    private final FraudCaseRepository fraudCaseRepository;

    private final ObjectMapper objectMapper;


    public FraudEvaluationService(
            List<FraudRule> rules,
            TransactionRepository transactionRepository,
            FraudCaseRepository fraudCaseRepository,
            ObjectMapper objectMapper
    ) {
        this.rules = rules;
        this.transactionRepository = transactionRepository;
        this.fraudCaseRepository = fraudCaseRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public EvaluateResponse evaluate(TransactionEventRequest request) {

        // Idempotency: if already evaluated, return stored result
        var existing = fraudCaseRepository.findByTransaction_TransactionId(request.transactionId());
        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        // Persist transaction
        TransactionEntity tx = new TransactionEntity();
        tx.setTransactionId(request.transactionId());
        tx.setCustomerId(request.customerId());
        tx.setAmount(request.amount());
        tx.setCurrency(request.currency());
        tx.setMerchant(request.merchant());
        tx.setCategory(request.category());
        tx.setEventTime(request.eventTime());

        // store raw payload (nice for debugging/audit)
        tx.setRawPayload(toJsonNodeQuietly(request));

        tx = transactionRepository.save(tx);

        // Evaluate rules
        TransactionEvent event = new TransactionEvent(
                request.transactionId(),
                request.customerId(),
                request.amount(),
                request.currency(),
                request.merchant(),
                request.category(),
                request.eventTime()
        );

        List<RuleHit> hits = rules.stream()
                .map(r -> r.evaluate(event))
                .flatMap(java.util.Optional::stream)
                .toList();

        int riskScore = calculateRiskScore(hits);
        boolean flagged = isFlagged(hits, riskScore);

        // Persist fraud case + hits
        FraudCaseEntity fraudCase = new FraudCaseEntity();
        fraudCase.setTransaction(tx);
        fraudCase.setRiskScore(riskScore);
        fraudCase.setFlagged(flagged);

        // attach hits
        for (RuleHit hit : hits) {
            RuleHitEntity e = new RuleHitEntity();
            e.setFraudCase(fraudCase);
            e.setRuleId(hit.ruleId());
            e.setRuleVersion(hit.ruleVersion());
            e.setSeverity(hit.severity().name());
            e.setReason(hit.reason());

            e.setMetadata(toJsonNodeQuietly(hit.metadata()));
            fraudCase.getRuleHits().add(e);
        }

        fraudCase = fraudCaseRepository.save(fraudCase);

        return mapToResponse(fraudCase);
    }

    public EvaluateResponse getCaseByTransactionId(String transactionId) {
        FraudCaseEntity fc = fraudCaseRepository.findByTransaction_TransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found for transactionId=" + transactionId));
        return mapToResponse(fc);
    }

    private int calculateRiskScore(List<RuleHit> hits) {
        // simple scoring (good enough for assignment; easy to explain in interview)
        int score = 0;
        for (RuleHit h : hits) {
            score += switch (h.severity()) {
                case LOW -> 10;
                case MEDIUM -> 30;
                case HIGH -> 70;
            };
        }
        return Math.min(score, 100);
    }

    private boolean isFlagged(List<RuleHit> hits, int riskScore) {
        boolean anyHigh = hits.stream().anyMatch(h -> h.severity() == Severity.HIGH);
        return anyHigh || riskScore >= 70;
    }

    private EvaluateResponse mapToResponse(FraudCaseEntity fc) {
        List<RuleHitDto> dtos = fc.getRuleHits().stream()
                .map(h -> new RuleHitDto(h.getRuleId(), h.getRuleVersion(), h.getSeverity(), h.getReason()))
                .toList();

        return new EvaluateResponse(
                fc.getTransaction().getTransactionId(),
                fc.isFlagged(),
                fc.getRiskScore(),
                dtos
        );
    }


    private JsonNode toJsonNodeQuietly(Object o) {
        try {
            return objectMapper.valueToTree(o);
        } catch (Exception e) {
            e.printStackTrace(); // TEMP - remove later
            return null;
        }
    }
}
