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
import java.time.OffsetDateTime;
import java.util.List;
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
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ACCOUNT));
        when(operationTypeRepository.findById(operationTypeId)).thenReturn(Optional.of(debitType));
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        transactionService.createTransaction(new CreateTransactionRequest(1L, operationTypeId, new BigDecimal("50.00")));

        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("-50.00"));
    }

    @Test
    void createTransaction_creditOperation_amountStoredPositive() {
        OperationType creditType = new OperationType(4L, "Credit Voucher", true);
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ACCOUNT));
        when(operationTypeRepository.findById(4L)).thenReturn(Optional.of(creditType));
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        transactionService.createTransaction(new CreateTransactionRequest(1L, 4L, new BigDecimal("123.45")));

        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("123.45"));
    }

    @Test
    void createTransaction_debitOperation_balanceDecreasedOnAccount() {
        OperationType debitType = new OperationType(1L, "Normal Purchase", false);
        Account account = new Account("12345678900");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(1L)).thenReturn(Optional.of(debitType));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        transactionService.createTransaction(new CreateTransactionRequest(1L, 1L, new BigDecimal("50.00")));

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("-50.00"));
    }

    @Test
    void createTransaction_creditOperation_balanceIncreasedOnAccount() {
        OperationType creditType = new OperationType(4L, "Credit Voucher", true);
        Account account = new Account("12345678900");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(4L)).thenReturn(Optional.of(creditType));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        transactionService.createTransaction(new CreateTransactionRequest(1L, 4L, new BigDecimal("100.00")));

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void createTransaction_multipleTransactions_balanceAccumulates() {
        Account account = new Account("12345678900");
        OperationType debit = new OperationType(1L, "Normal Purchase", false);
        OperationType credit = new OperationType(4L, "Credit Voucher", true);

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(1L)).thenReturn(Optional.of(debit));
        when(operationTypeRepository.findById(4L)).thenReturn(Optional.of(credit));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        transactionService.createTransaction(new CreateTransactionRequest(1L, 1L, new BigDecimal("50.00")));
        transactionService.createTransaction(new CreateTransactionRequest(1L, 4L, new BigDecimal("200.00")));
        transactionService.createTransaction(new CreateTransactionRequest(1L, 1L, new BigDecimal("30.00")));

        // -50 + 200 - 30 = 120
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("120.00"));
    }

    @Test
    void createTransaction_accountNotFound_throwsAccountNotFoundException() {
        when(accountRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            transactionService.createTransaction(new CreateTransactionRequest(99L, 1L, BigDecimal.TEN)))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void createTransaction_operationTypeNotFound_throwsOperationTypeNotFoundException() {
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ACCOUNT));
        when(operationTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            transactionService.createTransaction(new CreateTransactionRequest(1L, 99L, BigDecimal.TEN)))
            .isInstanceOf(OperationTypeNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void createTransaction_eventDateIsRecentlySet() {
        OperationType creditType = new OperationType(4L, "Credit Voucher", true);
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ACCOUNT));
        when(operationTypeRepository.findById(4L)).thenReturn(Optional.of(creditType));
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);
        transactionService.createTransaction(new CreateTransactionRequest(1L, 4L, new BigDecimal("10.00")));

        assertThat(captor.getValue().getEventDate()).isAfter(before);
    }

    @Test
    void discharge_payment60_partiallyCover() {

        OperationType creditType = new OperationType(4L, "Credit Voucher", true);

        OperationType debitType = new OperationType(1L, "Normal Purchase", false);
        Transaction tx1 = new Transaction(ACCOUNT, debitType, new BigDecimal("-50.00"));
        Transaction tx2 = new Transaction(ACCOUNT, debitType, new BigDecimal("-23.50"));
        Transaction tx3 = new Transaction(ACCOUNT, debitType, new BigDecimal("-18.70"));

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ACCOUNT));
        when(operationTypeRepository.findById(4L)).thenReturn(Optional.of(creditType));
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.findPendingDebitsForUpdate(1L)).thenReturn(List.of(tx1, tx2, tx3));
        var response = transactionService.createTransaction(new CreateTransactionRequest(1L, 4L, new BigDecimal("60.00")));


        assertThat(tx1.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(tx2.getBalance()).isEqualByComparingTo(new BigDecimal("-13.5"));
        assertThat(tx3.getBalance()).isEqualByComparingTo(new BigDecimal("-18.7"));
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
