package com.vinay.moneymanager.transaction.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinay.moneymanager.common.exception.GlobalExceptionHandler;
import com.vinay.moneymanager.config.SecurityConfig;
import com.vinay.moneymanager.security.entrypoint.JwtAuthenticationEntryPoint;
import com.vinay.moneymanager.security.jwt.JwtService;
import com.vinay.moneymanager.security.service.CustomUserDetailsService;
import com.vinay.moneymanager.transaction.dto.request.TransactionRequest;
import com.vinay.moneymanager.transaction.dto.response.TransactionResponse;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import com.vinay.moneymanager.transaction.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class TransactionControllerTest {

  private static final LocalDateTime TEST_TIME = LocalDateTime.of(2026, 7, 18, 12, 0);
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private TransactionService transactionService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  @MockitoBean private CustomUserDetailsService customUserDetailsService;

  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldCreateTransactionSuccessfully() throws Exception {
    TransactionRequest request = createTransactionRequest();
    TransactionResponse response = createTransactionResponse(request);
    when(transactionService.createTransaction(any(), anyString())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transaction was added successfully"))
        .andExpect(jsonPath("$.data.amount").value(request.getAmount()))
        .andExpect(jsonPath("$.data.categoryId").value(request.getCategoryId()));

    verify(transactionService)
        .createTransaction(any(TransactionRequest.class), eq("vinay@test.com"));
  }

  // GET /transactions/{id}
  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldReturnTransaction() throws Exception {
    TransactionRequest request = createTransactionRequest();
    TransactionResponse response = createTransactionResponse(request);

    when(transactionService.getTransaction(any(), anyString())).thenReturn(response);

    mockMvc
        .perform(get("/api/transactions/" + response.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transaction fetched successfully"))
        .andExpect(jsonPath("$.data.amount").value(request.getAmount()))
        .andExpect(jsonPath("$.data.categoryId").value(request.getCategoryId()));

    verify(transactionService).getTransaction(eq(response.getId()), eq("vinay@test.com"));
  }

  // GET /transactions
  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldReturnAllTransactions() throws Exception {
    List<TransactionResponse> transactions = getTransactionResponses();

    when(transactionService.getTransactions(anyString())).thenReturn(transactions);

    mockMvc
        .perform(get("/api/transactions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transactions fetched successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isNotEmpty());
    verify(transactionService).getTransactions(eq("vinay@test.com"));
  }

  // PUT /transactions/{id}
  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldUpdateTransactionSuccessfully() throws Exception {
    TransactionRequest updateRequest = createTransactionRequest();
    updateRequest.setAmount(BigDecimal.valueOf(200));
    updateRequest.setType(TransactionType.EXPENSE);
    updateRequest.setCategoryId(2);
    updateRequest.setDescription("Dining Out");
    TransactionResponse updateResponse = createTransactionResponse(updateRequest);

    when(transactionService.updateTransaction(any(), any(TransactionRequest.class), anyString()))
        .thenReturn(updateResponse);

    mockMvc
        .perform(
            put("/api/transactions/" + updateResponse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transaction was updated successfully"))
        .andExpect(jsonPath("$.data.amount").value(200))
        .andExpect(jsonPath("$.data.categoryId").value(2))
        .andExpect(jsonPath("$.data.description").value("Dining Out"));
    verify(transactionService)
        .updateTransaction(any(UUID.class), any(TransactionRequest.class), eq("vinay@test.com"));
  }

  // DELETE /transactions/{id}
  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldDeleteTransactionSuccessfully() throws Exception {

    UUID transactionId = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/transactions/" + transactionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));

    verify(transactionService).deleteTransaction(transactionId, "vinay@test.com");
  }

  // GET /transactions/category/{id}
  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldReturnTransactionsByCategory() throws Exception {
    List<TransactionResponse> transactionResponses = getTransactionResponses();
    int categoryId = 1;
    when(transactionService.getTransactionsByCategory(anyString(), anyInt()))
        .thenReturn(transactionResponses);
    mockMvc
        .perform(get("/api/transactions/category/" + categoryId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transactions fetched successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isNotEmpty());
    verify(transactionService).getTransactionsByCategory(eq("vinay@test.com"), eq(categoryId));
  }

  // GET /transactions/type/{type}
  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldReturnTransactionsByType() throws Exception {
    List<TransactionResponse> transactionResponses = getTransactionResponses();
    TransactionType transactionType = TransactionType.EXPENSE;
    when(transactionService.getTransactionsByType(anyString(), any(TransactionType.class)))
        .thenReturn(transactionResponses);

    mockMvc
        .perform(get("/api/transactions/type/" + transactionType))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transactions fetched successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isNotEmpty())
        .andExpect(jsonPath("$.data[1].type").value("EXPENSE"));

    verify(transactionService).getTransactionsByType(eq("vinay@test.com"), eq(transactionType));
  }

  // Validation
  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {

    TransactionRequest request = createTransactionRequest();
    request.setAmount(BigDecimal.valueOf(-1));

    mockMvc
        .perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors.[0].field").value("amount"))
        .andExpect(jsonPath("$.errors.[0].message").value("Amount must be greater than zero"));
    verify(transactionService, never())
        .createTransaction(any(TransactionRequest.class), anyString());
  }

  private List<TransactionResponse> getTransactionResponses() {
    List<TransactionResponse> transactionResponseList = new ArrayList<>();
    transactionResponseList.add(createTransactionResponse(createTransactionRequest()));
    transactionResponseList.add(createTransactionResponse(createTransactionRequest()));
    transactionResponseList.add(createTransactionResponse(createTransactionRequest()));
    return transactionResponseList;
  }

  private TransactionResponse createTransactionResponse(TransactionRequest request) {
    return createTransactionResponse(
        request.getAmount(),
        request.getDescription(),
        request.getType(),
        request.getCategoryId(),
        request.getDescription());
  }

  private TransactionResponse createTransactionResponse(
      BigDecimal amount,
      String description,
      TransactionType type,
      Integer categoryId,
      String categoryName) {
    return TransactionResponse.builder()
        .id(UUID.randomUUID())
        .amount(amount)
        .description(description)
        .type(type)
        .categoryId(categoryId)
        .categoryName(categoryName)
        .createdAt(TEST_TIME)
        .updatedAt(TEST_TIME)
        .build();
  }

  private TransactionRequest createTransactionRequest() {
    return TransactionRequest.builder()
        .amount(new BigDecimal("150.62"))
        .type(TransactionType.EXPENSE)
        .categoryId(1)
        .description("Uber auto to Railway Station")
        .transactionDate(TEST_TIME)
        .build();
  }
}
