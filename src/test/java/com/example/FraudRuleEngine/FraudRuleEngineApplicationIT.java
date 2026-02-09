package com.example.FraudRuleEngine;

import com.example.FraudRuleEngine.api.dto.EvaluateResponse;
import com.example.FraudRuleEngine.api.dto.TransactionEventRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
class FraudRuleEngineApplicationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("fraud")
            .withUsername("fraud")
            .withPassword("fraud");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Ensure Flyway runs and schema matches entities
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    TestRestTemplate rest;

    @Test
    void evaluate_highAmount_flags_and_can_retrieve_case() {
        String txId = "tx-it-high-001";

        var req = new TransactionEventRequest(
                txId,
                "cust-1",
                new BigDecimal("70000"),
                "ZAR",
                "SHOPRITE",
                "groceries",
                OffsetDateTime.parse("2026-02-01T10:00:00Z")
        );

        EvaluateResponse resp = rest.postForObject("/v1/transactions/evaluate", req, EvaluateResponse.class);

        assertNotNull(resp);
        assertEquals(txId, resp.transactionId());
        assertTrue(resp.flagged());
        assertTrue(resp.riskScore() >= 70);
        assertNotNull(resp.ruleHits());
        assertFalse(resp.ruleHits().isEmpty());

        // Retrieve stored case
        EvaluateResponse stored = rest.getForObject("/v1/cases/" + txId, EvaluateResponse.class);

        assertNotNull(stored);
        assertEquals(txId, stored.transactionId());
        assertTrue(stored.flagged());
        assertEquals(resp.riskScore(), stored.riskScore());
        assertNotNull(stored.ruleHits());
        assertFalse(stored.ruleHits().isEmpty());
    }

    @Test
    void evaluate_lowAmount_not_flagged() {
        String txId = "tx-it-low-001";

        var req = new TransactionEventRequest(
                txId,
                "cust-1",
                new BigDecimal("1000"),
                "ZAR",
                "CHECKERS",
                "groceries",
                OffsetDateTime.parse("2026-02-01T10:00:00Z")
        );

        EvaluateResponse resp = rest.postForObject("/v1/transactions/evaluate", req, EvaluateResponse.class);

        assertNotNull(resp);
        assertEquals(txId, resp.transactionId());
        assertFalse(resp.flagged());
        assertEquals(0, resp.riskScore());
        assertNotNull(resp.ruleHits());
    }
}
