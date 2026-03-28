package com.example.transactions.controller;

import com.example.transactions.dto.AccountResponse;
import com.example.transactions.dto.CreateAccountRequest;
import com.example.transactions.exception.AccountNotFoundException;
import com.example.transactions.exception.DuplicateDocumentException;
import com.example.transactions.exception.GlobalExceptionHandler;
import com.example.transactions.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AccountService accountService;

    @Test
    void createAccount_validRequest_returns201() throws Exception {
        when(accountService.createAccount(any())).thenReturn(new AccountResponse(1L, "12345678900"));

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateAccountRequest("12345678900"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.account_id").value(1))
            .andExpect(jsonPath("$.document_number").value("12345678900"));
    }

    @Test
    void createAccount_duplicateDocument_returns409() throws Exception {
        when(accountService.createAccount(any())).thenThrow(new DuplicateDocumentException("12345678900"));

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateAccountRequest("12345678900"))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createAccount_missingBody_returns422() throws Exception {
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createAccount_nonNumericDocument_returns422() throws Exception {
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateAccountRequest("abc12345678"))))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getAccount_existingAccount_returns200() throws Exception {
        when(accountService.getAccount(1L)).thenReturn(new AccountResponse(1L, "12345678900"));

        mockMvc.perform(get("/accounts/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.account_id").value(1))
            .andExpect(jsonPath("$.document_number").value("12345678900"));
    }

    @Test
    void getAccount_nonExistentAccount_returns404() throws Exception {
        when(accountService.getAccount(99L)).thenThrow(new AccountNotFoundException(99L));

        mockMvc.perform(get("/accounts/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Account not found with id: 99"));
    }
}
