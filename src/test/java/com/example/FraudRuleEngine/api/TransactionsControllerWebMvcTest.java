package com.example.FraudRuleEngine.api;

import com.example.FraudRuleEngine.api.dto.EvaluateResponse;
import com.example.FraudRuleEngine.service.FraudEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionsController.class)
@Import(ApiExceptionHandler.class) // ensures your global handler is active in WebMvc slice tests
class TransactionsControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FraudEvaluationService service;

    @Test
    void evaluate_shouldReturn200_andJsonPayload() throws Exception {
        when(service.evaluate(any()))
                .thenReturn(new EvaluateResponse("tx-it-1", false, 0, List.of()));

        mockMvc.perform(
                        post("/v1/transactions/evaluate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "transactionId": "tx-it-1",
                                  "customerId": "cust-it-1",
                                  "amount": 100.00,
                                  "currency": "USD",
                                  "merchant": "SAFE_MERCHANT",
                                  "category": "RETAIL",
                                  "eventTime": "2026-02-06T11:00:00Z"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value("tx-it-1"))
                .andExpect(jsonPath("$.flagged").value(false))
                .andExpect(jsonPath("$.riskScore").value(0))
                .andExpect(jsonPath("$.ruleHits").isArray());
    }

    @Test
    void evaluate_shouldReturn400_whenBodyValidationFails() throws Exception {
        // missing required fields (customerId, merchant, category, eventTime etc.)
        mockMvc.perform(
                        post("/v1/transactions/evaluate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "transactionId": "",
                                  "amount": -1,
                                  "currency": ""
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void getCase_shouldReturn400_whenTransactionIdTooLong() throws Exception {
        String tooLong = "x".repeat(65);

        mockMvc.perform(get("/v1/cases/" + tooLong))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }
}
