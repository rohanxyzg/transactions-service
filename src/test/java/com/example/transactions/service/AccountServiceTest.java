package com.example.transactions.service;

import com.example.transactions.domain.Account;
import com.example.transactions.dto.AccountResponse;
import com.example.transactions.dto.CreateAccountRequest;
import com.example.transactions.exception.AccountNotFoundException;
import com.example.transactions.exception.DuplicateDocumentException;
import com.example.transactions.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_newDocument_returnsCreatedAccount() {
        when(accountRepository.existsByDocumentNumber("12345678900")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.createAccount(new CreateAccountRequest("12345678900"));

        assertThat(response.documentNumber()).isEqualTo("12345678900");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_duplicateDocument_throwsDuplicateDocumentException() {
        when(accountRepository.existsByDocumentNumber("12345678900")).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(new CreateAccountRequest("12345678900")))
            .isInstanceOf(DuplicateDocumentException.class)
            .hasMessageContaining("12345678900");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void getAccount_existingId_returnsAccount() {
        Account account = new Account("12345678900");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount(1L);

        assertThat(response.documentNumber()).isEqualTo("12345678900");
    }

    @Test
    void getAccount_nonExistentId_throwsAccountNotFoundException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(99L))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessageContaining("99");
    }
}
