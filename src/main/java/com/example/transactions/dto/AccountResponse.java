package com.example.transactions.dto;

import com.example.transactions.domain.Account;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountResponse(
    @JsonProperty("account_id")
    Long accountId,

    @JsonProperty("document_number")
    String documentNumber
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getDocumentNumber());
    }
}
