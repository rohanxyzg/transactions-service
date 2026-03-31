package com.example.transactions;

import com.example.transactions.domain.Account;
import com.example.transactions.domain.OperationType;
import com.example.transactions.dto.CreateAccountRequest;
import com.example.transactions.dto.CreateTransactionRequest;
import com.example.transactions.repository.AccountRepository;
import com.example.transactions.repository.OperationTypeRepository;
import com.example.transactions.service.AccountService;
import com.example.transactions.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the pessimistic write lock on Account prevents lost updates
 * when multiple transactions are created concurrently for the same account.
 *
 * Without SELECT FOR UPDATE, two threads could read the same balance,
 * each compute a new value, and one overwrite the other — leaving the balance wrong.
 * With the lock, each thread waits its turn, reads the latest committed balance,
 * and the final result is always exactly N * amount.
 */
@SpringBootTest
class ConcurrentTransactionTest {

    @Autowired private TransactionService transactionService;
    @Autowired private AccountService accountService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private OperationTypeRepository operationTypeRepository;

    @BeforeEach
    void seedOperationTypes() {
        if (!operationTypeRepository.existsById(1L)) {
            operationTypeRepository.save(new OperationType(1L, "Normal Purchase", false));
        }
    }

    @Test
    void concurrentDebits_pessimisticLock_preventsLostUpdates() throws Exception {
        Long accountId = accountService.createAccount(
            new CreateAccountRequest("11111111111")).accountId();

        int threads = 10;
        BigDecimal debitAmount = new BigDecimal("10.00");

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        List<Exception> errors = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startGate.await(); // all threads wait here, then race together
                    transactionService.createTransaction(
                        new CreateTransactionRequest(accountId, 1L, debitAmount));
                } catch (Exception e) {
                    synchronized (errors) { errors.add(e); }
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        startGate.countDown(); // release all threads at once
        doneLatch.await();
        executor.shutdown();

        assertThat(errors).isEmpty();

        BigDecimal expectedBalance = debitAmount.negate().multiply(BigDecimal.valueOf(threads)); // -100.00
        Account finalAccount = accountRepository.findById(accountId).orElseThrow();
        assertThat(finalAccount.getBalance()).isEqualByComparingTo(expectedBalance);
    }
}
