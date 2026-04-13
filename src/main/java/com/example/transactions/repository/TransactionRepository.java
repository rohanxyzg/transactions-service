package com.example.transactions.repository;

import com.example.transactions.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // remaining debits for update

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId AND t.balance < 0 order by t.eventDate asc")
    List<Transaction> findPendingDebitsForUpdate(@Param("accountId") Long accountId);
}
