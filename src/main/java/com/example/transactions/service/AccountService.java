package com.example.transactions.service;

import com.example.transactions.domain.Account;
import com.example.transactions.dto.AccountResponse;
import com.example.transactions.dto.CreateAccountRequest;
import com.example.transactions.exception.AccountNotFoundException;
import com.example.transactions.exception.DuplicateDocumentException;
import com.example.transactions.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (accountRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new DuplicateDocumentException(request.documentNumber());
        }
        Account saved = accountRepository.save(new Account(request.documentNumber()));
        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        return accountRepository.findById(accountId)
            .map(AccountResponse::from)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
