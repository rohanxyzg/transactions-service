package com.example.transactions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateTransactionRequest(
    @JsonProperty("account_id")
    @NotNull(message = "account_id is required")
    Long accountId,

    @JsonProperty("operation_type_id")
    @NotNull(message = "operation_type_id is required")
    Long operationTypeId,

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "amount must have at most 2 decimal places")
    BigDecimal amount
) {}
