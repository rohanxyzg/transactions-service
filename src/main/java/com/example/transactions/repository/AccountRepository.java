package com.example.transactions.repository;

import com.example.transactions.domain.Account;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByDocumentNumber(String documentNumber);

    /**
     * Locks the account row with SELECT FOR UPDATE before returning it.
     * Ensures that concurrent transactions on the same account are serialised —
     * each transaction reads the latest committed balance and applies its change
     * without any other transaction interleaving between the read and the write.
     *
     * Lock timeout is 5 seconds: if another transaction already holds the lock,
     * we fail fast rather than queueing indefinitely.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);
}
