package com.example.transactions.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_type_id", nullable = false)
    private OperationType operationType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "event_date", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime eventDate;

    protected Transaction() {}

    public Transaction(Account account, OperationType operationType, BigDecimal amount) {
        this.account = account;
        this.operationType = operationType;
        this.amount = amount;
        this.eventDate = OffsetDateTime.now();
    }

    public Long getId() { return id; }

    public Account getAccount() { return account; }

    public OperationType getOperationType() { return operationType; }

    public BigDecimal getAmount() { return amount; }

    public OffsetDateTime getEventDate() { return eventDate; }
}
