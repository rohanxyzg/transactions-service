package com.example.transactions.service;

import com.example.transactions.domain.Account;
import com.example.transactions.domain.OperationType;
import com.example.transactions.domain.Transaction;
import com.example.transactions.dto.CreateTransactionRequest;
import com.example.transactions.dto.TransactionResponse;
import com.example.transactions.exception.AccountNotFoundException;
import com.example.transactions.exception.OperationTypeNotFoundException;
import com.example.transactions.repository.AccountRepository;
import com.example.transactions.repository.OperationTypeRepository;
import com.example.transactions.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OperationTypeRepository operationTypeRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            OperationTypeRepository operationTypeRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.operationTypeRepository = operationTypeRepository;
    }

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        // Acquire a pessimistic write lock on the account row.
        // This prevents two concurrent transactions on the same account from reading
        // the same balance and each computing an incorrect new value (lost update).
        Account account = accountRepository.findByIdForUpdate(request.accountId())
            .orElseThrow(() -> new AccountNotFoundException(request.accountId()));

        OperationType operationType = operationTypeRepository.findById(request.operationTypeId())
            .orElseThrow(() -> new OperationTypeNotFoundException(request.operationTypeId()));

        BigDecimal signedAmount = applySign(request.amount(), operationType);

        // Update the account balance within the same transaction and lock.
        // Hibernate dirty-checking will flush the balance update automatically on commit.
        account.applyTransaction(signedAmount);

        Transaction saved = transactionRepository.save(new Transaction(account, operationType, signedAmount));
        return TransactionResponse.from(saved);
    }

    /**
     * Debit operations (purchase, withdrawal) are stored as negative values.
     * Credit operations are stored as positive values.
     * The input amount is always treated as an absolute value.
     */
    private BigDecimal applySign(BigDecimal amount, OperationType operationType) {
        BigDecimal absolute = amount.abs();
        return operationType.isCredit() ? absolute : absolute.negate();
    }
}
