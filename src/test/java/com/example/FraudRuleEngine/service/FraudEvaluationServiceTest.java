package com.example.FraudRuleEngine.service;

import com.example.FraudRuleEngine.api.dto.EvaluateResponse;
import com.example.FraudRuleEngine.api.dto.TransactionEventRequest;
import com.example.FraudRuleEngine.domain.rules.FraudRule;
import com.example.FraudRuleEngine.domain.rules.HighAmountRule;
import com.example.FraudRuleEngine.persistence.entity.FraudCaseEntity;
import com.example.FraudRuleEngine.persistence.entity.TransactionEntity;
import com.example.FraudRuleEngine.persistence.repo.FraudCaseRepository;
import com.example.FraudRuleEngine.persistence.repo.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FraudEvaluationServiceTest {

    @Test
    void evaluate_persistsTransactionAndCreatesFraudCase_whenRuleHitsExist() {
        // Arrange
        TransactionRepository txRepo = mock(TransactionRepository.class);
        FraudCaseRepository caseRepo = mock(FraudCaseRepository.class);

        List<FraudRule> rules = List.of(new HighAmountRule(new BigDecimal("50000")));
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        FraudEvaluationService service = new FraudEvaluationService(rules, txRepo, caseRepo, objectMapper);

        String txId = "tx-test-" + UUID.randomUUID().toString().substring(0, 8);

        when(caseRepo.findByTransaction_TransactionId(txId)).thenReturn(Optional.empty());
        when(txRepo.save(any(TransactionEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(caseRepo.save(any(FraudCaseEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionEventRequest req = new TransactionEventRequest(
                txId,
                "cust-1",
                new BigDecimal("70000"),
                "ZAR",
                "SHOPRITE",
                "groceries",
                OffsetDateTime.parse("2026-02-01T10:00:00Z")
        );

        // Act
        EvaluateResponse resp = service.evaluate(req);

        // Assert response
        assertEquals(txId, resp.transactionId());
        assertTrue(resp.flagged());
        assertEquals(70, resp.riskScore());
        assertEquals(1, resp.ruleHits().size());
        assertEquals("HIGH_AMOUNT", resp.ruleHits().get(0).ruleId());

        // Assert raw payload saved
        ArgumentCaptor<TransactionEntity> txCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(txRepo).save(txCaptor.capture());
        assertNotNull(txCaptor.getValue().getRawPayload(), "raw_payload should be saved as JSONB");

        // Assert fraud case saved with hits
        ArgumentCaptor<FraudCaseEntity> caseCaptor = ArgumentCaptor.forClass(FraudCaseEntity.class);
        verify(caseRepo).save(caseCaptor.capture());
        FraudCaseEntity savedCase = caseCaptor.getValue();

        assertNotNull(savedCase.getTransaction());
        assertEquals(txId, savedCase.getTransaction().getTransactionId());
        assertEquals(70, savedCase.getRiskScore());
        assertTrue(savedCase.isFlagged());
        assertEquals(1, savedCase.getRuleHits().size());
        assertEquals("HIGH_AMOUNT", savedCase.getRuleHits().get(0).getRuleId());
    }
}
