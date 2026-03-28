package com.example.transactions.exception;

public class DuplicateDocumentException extends RuntimeException {

    public DuplicateDocumentException(String documentNumber) {
        super("An account already exists for document number: " + documentNumber);
    }
}
