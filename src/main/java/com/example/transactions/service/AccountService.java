package com.example.transactions.service;

import com.example.transactions.domain.Account;
import com.example.transactions.dto.AccountResponse;
import com.example.transactions.dto.CreateAccountRequest;
import com.example.transactions.exception.AccountNotFoundException;
import com.example.transactions.exception.DuplicateDocumentException;
import com.example.transactions.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (accountRepository.existsByDocumentNumber(request.documentNumber())) {
            log.warn("Duplicate account creation attempt for an already registered document");
            throw new DuplicateDocumentException(request.documentNumber());
        }
        Account saved = accountRepository.save(new Account(request.documentNumber()));
        log.info("Account created with id={}", saved.getId());
        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        log.debug("Fetching account id={}", accountId);
        return accountRepository.findById(accountId)
            .map(AccountResponse::from)
            .orElseThrow(() -> {
                log.warn("Account not found with id={}", accountId);
                return new AccountNotFoundException(accountId);
            });
    }
}
