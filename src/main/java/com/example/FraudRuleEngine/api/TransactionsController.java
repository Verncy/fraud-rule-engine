package com.example.FraudRuleEngine.api;

import com.example.FraudRuleEngine.api.dto.EvaluateResponse;
import com.example.FraudRuleEngine.api.dto.TransactionEventRequest;
import com.example.FraudRuleEngine.service.FraudEvaluationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@Validated
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
    public ResponseEntity<EvaluateResponse> getCase(
            @PathVariable
            @NotBlank(message = "transactionId is required")
            @Size(max = 64, message = "transactionId must be <= 64 characters")
            String transactionId
    ) {
        return ResponseEntity.ok(service.getCaseByTransactionId(transactionId));
    }
}
