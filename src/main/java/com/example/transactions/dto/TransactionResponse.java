package com.example.transactions.dto;

import com.example.transactions.domain.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionResponse(
    @JsonProperty("transaction_id")
    Long transactionId,

    @JsonProperty("account_id")
    Long accountId,

    @JsonProperty("operation_type_id")
    Long operationTypeId,

    BigDecimal amount,

    @JsonProperty("event_date")
    OffsetDateTime eventDate
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
            t.getId(),
            t.getAccount().getId(),
            t.getOperationType().getId(),
            t.getAmount(),
            t.getEventDate()
        );
    }
}
