package com.example.FraudRuleEngine.api;

import com.example.FraudRuleEngine.api.dto.EvaluateResponse;
import com.example.FraudRuleEngine.api.dto.TransactionEventRequest;
import com.example.FraudRuleEngine.service.FraudEvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class TransactionsController {

    private final FraudEvaluationService service;

    public TransactionsController(FraudEvaluationService service) {
        this.service = service;
    }

    @PostMapping("/transactions/evaluate")
    public ResponseEntity<EvaluateResponse> evaluate(@Valid @RequestBody TransactionEventRequest request) {
        return ResponseEntity.ok(service.evaluate(request));
    }

    @GetMapping("/cases/{transactionId}")
    public ResponseEntity<EvaluateResponse> getCase(@PathVariable String transactionId) {
        return ResponseEntity.ok(service.getCaseByTransactionId(transactionId));
    }
}
