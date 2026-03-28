package com.example.transactions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAccountRequest(
    @JsonProperty("document_number")
    @NotBlank(message = "document_number is required")
    @Pattern(regexp = "\\d{11}", message = "document_number must be exactly 11 digits")
    String documentNumber
) {}
