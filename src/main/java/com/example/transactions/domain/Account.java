package com.example.transactions.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_accounts_document_number", columnNames = "document_number")
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    protected Account() {}

    public Account(String documentNumber) {
        this.documentNumber = documentNumber;
        this.balance = BigDecimal.ZERO;
    }

    /**
     * Applies a signed transaction amount to the account balance.
     * Debit amounts are already negative; credit amounts are positive.
     * Called within a transaction that holds a pessimistic write lock on this account,
     * so concurrent updates are serialised at the DB level.
     */
    public void applyTransaction(BigDecimal signedAmount) {
        this.balance = this.balance.add(signedAmount);
    }

    public Long getId() { return id; }

    public String getDocumentNumber() { return documentNumber; }

    public BigDecimal getBalance() { return balance; }
}
