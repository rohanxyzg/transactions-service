package com.example.transactions.domain;

import jakarta.persistence.*;

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

    protected Account() {}

    public Account(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Long getId() { return id; }

    public String getDocumentNumber() { return documentNumber; }
}
