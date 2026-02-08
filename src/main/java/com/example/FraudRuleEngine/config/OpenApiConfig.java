package com.example.FraudRuleEngine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fraudRuleEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fraud Rule Engine API")
                        .version("v1")
                        .description("Evaluates transactions and creates fraud cases when flagged."));
    }
}
