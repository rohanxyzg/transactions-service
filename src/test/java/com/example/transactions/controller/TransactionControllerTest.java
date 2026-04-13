package com.example.transactions.controller;

import com.example.transactions.dto.CreateTransactionRequest;
import com.example.transactions.dto.TransactionResponse;
import com.example.transactions.exception.AccountNotFoundException;
import com.example.transactions.exception.GlobalExceptionHandler;
import com.example.transactions.exception.OperationTypeNotFoundException;
import com.example.transactions.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private TransactionService transactionService;

//    @Test
//    void createTransaction_validRequest_returns201() throws Exception {
//        TransactionResponse response = new TransactionResponse(
//            1L, 1L, 4L, new BigDecimal("123.45"), OffsetDateTime.now()
//        );
//        when(transactionService.createTransaction(any())).thenReturn(response);
//
//        mockMvc.perform(post("/transactions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(
//                    new CreateTransactionRequest(1L, 4L, new BigDecimal("123.45")))))
//            .andExpect(status().isCreated())
//            .andExpect(jsonPath("$.transaction_id").value(1))
//            .andExpect(jsonPath("$.amount").value(123.45));
//    }
//
//    @Test
//    void createTransaction_accountNotFound_returns404() throws Exception {
//        when(transactionService.createTransaction(any())).thenThrow(new AccountNotFoundException(1L));
//
//        mockMvc.perform(post("/transactions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(
//                    new CreateTransactionRequest(1L, 1L, new BigDecimal("10.00")))))
//            .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void createTransaction_operationTypeNotFound_returns404() throws Exception {
//        when(transactionService.createTransaction(any())).thenThrow(new OperationTypeNotFoundException(99L));
//
//        mockMvc.perform(post("/transactions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(
//                    new CreateTransactionRequest(1L, 99L, new BigDecimal("10.00")))))
//            .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void createTransaction_missingFields_returns422() throws Exception {
//        mockMvc.perform(post("/transactions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("{}"))
//            .andExpect(status().isUnprocessableEntity());
//    }
//
//    @Test
//    void createTransaction_zeroAmount_returns422() throws Exception {
//        mockMvc.perform(post("/transactions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(
//                    new CreateTransactionRequest(1L, 1L, BigDecimal.ZERO))))
//            .andExpect(status().isUnprocessableEntity());
//    }
//
//    @Test
//    void createTransaction_negativeAmount_returns422() throws Exception {
//        mockMvc.perform(post("/transactions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(
//                    new CreateTransactionRequest(1L, 1L, new BigDecimal("-10.00")))))
//            .andExpect(status().isUnprocessableEntity());
//    }
//
//    @Test
//    void createTransaction_amountAboveMax_returns422() throws Exception {
//        mockMvc.perform(post("/transactions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(
//                    new CreateTransactionRequest(1L, 1L, new BigDecimal("1000000.00")))))
//            .andExpect(status().isUnprocessableEntity());
//    }
}
