package com.example.transactions.service;

import com.example.transactions.domain.Account;
import com.example.transactions.domain.OperationType;
import com.example.transactions.domain.Transaction;
import com.example.transactions.dto.CreateTransactionRequest;
import com.example.transactions.exception.AccountNotFoundException;
import com.example.transactions.exception.OperationTypeNotFoundException;
import com.example.transactions.repository.AccountRepository;
import com.example.transactions.repository.OperationTypeRepository;
import com.example.transactions.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private OperationTypeRepository operationTypeRepository;

    @InjectMocks
    private TransactionService transactionService;

    private static final Account ACCOUNT = new Account("12345678900");

    @ParameterizedTest(name = "operationTypeId={0} should store a negative amount")
    @ValueSource(longs = {1L, 2L, 3L})
    void createTransaction_debitOperations_amountStoredNegative(Long operationTypeId) {
        OperationType debitType = new OperationType(operationTypeId, "Debit", false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(ACCOUNT));
        when(operationTypeRepository.findById(operationTypeId)).thenReturn(Optional.of(debitType));
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        transactionService.createTransaction(new CreateTransactionRequest(1L, operationTypeId, new BigDecimal("50.00")));

        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("-50.00"));
    }

    @Test
    void createTransaction_creditOperation_amountStoredPositive() {
        OperationType creditType = new OperationType(4L, "Credit Voucher", true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(ACCOUNT));
        when(operationTypeRepository.findById(4L)).thenReturn(Optional.of(creditType));
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        transactionService.createTransaction(new CreateTransactionRequest(1L, 4L, new BigDecimal("123.45")));

        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("123.45"));
    }

    @Test
    void createTransaction_accountNotFound_throwsAccountNotFoundException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            transactionService.createTransaction(new CreateTransactionRequest(99L, 1L, BigDecimal.TEN)))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void createTransaction_operationTypeNotFound_throwsOperationTypeNotFoundException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(ACCOUNT));
        when(operationTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            transactionService.createTransaction(new CreateTransactionRequest(1L, 99L, BigDecimal.TEN)))
            .isInstanceOf(OperationTypeNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void createTransaction_eventDateIsSetOnCreation() {
        OperationType creditType = new OperationType(4L, "Credit Voucher", true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(ACCOUNT));
        when(operationTypeRepository.findById(4L)).thenReturn(Optional.of(creditType));
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        transactionService.createTransaction(new CreateTransactionRequest(1L, 4L, new BigDecimal("10.00")));

        assertThat(captor.getValue().getEventDate()).isNotNull();
    }
}
