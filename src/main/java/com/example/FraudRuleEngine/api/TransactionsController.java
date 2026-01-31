package com.example.FraudRuleEngine.api;

import com.example.FraudRuleEngine.api.dto.EvaluateResponse;
import com.example.FraudRuleEngine.api.dto.RuleHitDto;
import com.example.FraudRuleEngine.api.dto.TransactionEventRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/transactions")
public class TransactionsController {

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluateResponse> evaluate(@Valid @RequestBody TransactionEventRequest request) {

        // Dummy response for now (we’ll replace with real rules + DB)
        EvaluateResponse response = new EvaluateResponse(
                request.transactionId(),
                false,
                0,
                List.of(
                        new RuleHitDto("DUMMY_RULE", "1.0", "LOW", "Placeholder hit (remove once rules are implemented)")
                )
        );

        return ResponseEntity.ok(response);
    }
}
