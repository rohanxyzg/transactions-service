package com.example.transactions.exception;

public class OperationTypeNotFoundException extends RuntimeException {

    public OperationTypeNotFoundException(Long operationTypeId) {
        super("Operation type not found with id: " + operationTypeId);
    }
}
