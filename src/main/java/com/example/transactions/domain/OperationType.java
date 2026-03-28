package com.example.transactions.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "operation_types")
public class OperationType {

    @Id
    @Column(name = "operation_type_id")
    private Long id;

    @Column(name = "description", nullable = false, length = 50)
    private String description;

    @Column(name = "is_credit", nullable = false)
    private boolean credit;

    protected OperationType() {}

    // Package-private constructor for use in tests
    OperationType(Long id, String description, boolean credit) {
        this.id = id;
        this.description = description;
        this.credit = credit;
    }

    public Long getId() { return id; }

    public String getDescription() { return description; }

    public boolean isCredit() { return credit; }
}
