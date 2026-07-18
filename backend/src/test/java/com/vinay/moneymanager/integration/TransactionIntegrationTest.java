package com.vinay.moneymanager.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinay.moneymanager.transaction.dto.request.TransactionRequest;
import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.transaction.entity.Transaction;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import com.vinay.moneymanager.transaction.repository.CategoryRepository;
import com.vinay.moneymanager.transaction.repository.TransactionRepository;
import com.vinay.moneymanager.user.dto.request.LoginRequest;
import com.vinay.moneymanager.user.dto.request.RegisterRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TransactionIntegrationTest {

  public static final String EMAIL = "vinay@test.com";
  public static final String TRANSACTIONS_CONTEXT_PATH = "/api/transactions";
  private static final String PASSWORD = "Vinay123";
  private static final LocalDateTime TEST_TIME = LocalDateTime.of(2026, Month.JULY, 18, 12, 0);

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TransactionRepository transactionRepository;
  @Autowired private CategoryRepository categoryRepository;

  private String jwtToken;

  @BeforeEach
  void setup() throws Exception {
    jwtToken = registerUserAndGetJwt();
  }

  // Security
  @Test
  void shouldReturnUnauthorizedWithoutJwt() throws Exception {
    UUID transactionId = UUID.randomUUID();
    mockMvc
        .perform(get(TRANSACTIONS_CONTEXT_PATH + "/" + transactionId))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }

  // Create
  @Test
  void shouldCreateTransactionSuccessfully() throws Exception {
    TransactionRequest request = createTransactionRequest();
    MvcResult result =
        performAuthenticatedPost(jwtToken, request)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Transaction was added successfully"))
            .andExpect(jsonPath("$.data.amount").value(request.getAmount()))
            .andExpect(jsonPath("$.data.type").value(TransactionType.EXPENSE.name()))
            .andExpect(jsonPath("$.data.description").value(request.getDescription()))
            .andExpect(jsonPath("$.data.categoryId").value(request.getCategoryId()))
            .andReturn();
    String responseBody = result.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    UUID transactionId = UUID.fromString(jsonNode.path("data").path("id").asText());
    assertTrue(transactionRepository.existsById(transactionId));
  }

  @Test
  void shouldReturnBadRequestForInvalidRequest() throws Exception {
    TransactionRequest request = createTransactionRequest();
    request.setAmount(BigDecimal.valueOf(-1));
    performAuthenticatedPost(jwtToken, request)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors[0].field").value("amount"))
        .andExpect(jsonPath("$.errors[0].message").value("Amount must be greater than zero"));
    assertEquals(0, transactionRepository.count());
  }

  // Read
  @Test
  void shouldGetTransactionSuccessfully() throws Exception {
    UUID transactionId = createTransactionAndReturnId();
    performAuthenticatedGet(jwtToken, TRANSACTIONS_CONTEXT_PATH + "/" + transactionId)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transaction fetched successfully"))
        .andExpect(jsonPath("$.data.id").value(transactionId.toString()))
        .andExpect(jsonPath("$.data.type").value(TransactionType.EXPENSE.name()))
        .andExpect(jsonPath("$.data.description").value("Uber auto to Railway Station"));
  }

  @Test
  void shouldGetAllTransactionsSuccessfully() throws Exception {
    UUID transactionId = createTransactionAndReturnId();
    performAuthenticatedGet(jwtToken, TRANSACTIONS_CONTEXT_PATH)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transactions fetched successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isNotEmpty())
        .andExpect(jsonPath("$.data[0].id").value(transactionId.toString()));
  }

  @Test
  void shouldReturnNotFoundForInvalidTransactionId() throws Exception {
    UUID transactionId = UUID.randomUUID();
    performAuthenticatedGet(jwtToken, TRANSACTIONS_CONTEXT_PATH + "/" + transactionId)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Transaction not found"));
  }

  // Update
  @Test
  void shouldUpdateTransactionSuccessfully() throws Exception {
    UUID transactionId = createTransactionAndReturnId();
    TransactionRequest request = createTransactionRequest();
    request.setAmount(BigDecimal.valueOf(65));
    mockMvc
        .perform(
            put(TRANSACTIONS_CONTEXT_PATH + "/" + transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transaction was updated successfully"))
        .andExpect(jsonPath("$.data.id").value(transactionId.toString()))
        .andExpect(jsonPath("$.data.amount").value(BigDecimal.valueOf(65)));
    Transaction transaction = transactionRepository.findById(transactionId).orElseThrow();
    assertEquals(BigDecimal.valueOf(65), transaction.getAmount());
  }

  // Delete
  @Test
  void shouldDeleteTransactionSuccessfully() throws Exception {
    UUID transactionId = createTransactionAndReturnId();
    mockMvc
        .perform(
            delete(TRANSACTIONS_CONTEXT_PATH + "/" + transactionId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    assertFalse(transactionRepository.existsById(transactionId));
  }

  // Filters
  @Test
  void shouldGetTransactionsByCategorySuccessfully() throws Exception {
    UUID transactionId = createTransactionAndReturnId();
    Category category = categoryRepository.findByName("Travel");
    performAuthenticatedGet(jwtToken, TRANSACTIONS_CONTEXT_PATH + "/category/" + category.getId())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transactions fetched successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isNotEmpty())
        .andExpect(jsonPath("$.data[0].categoryId").value(category.getId()))
        .andExpect(jsonPath("$.data[0].id").value(transactionId.toString()));
  }

  @Test
  void shouldGetTransactionsByTypeSuccessfully() throws Exception {
    UUID transactionId = createTransactionAndReturnId();
    performAuthenticatedGet(
            jwtToken, TRANSACTIONS_CONTEXT_PATH + "/type/" + TransactionType.EXPENSE.name())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Transactions fetched successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isNotEmpty())
        .andExpect(jsonPath("$.data[0].id").value(transactionId.toString()))
        .andExpect(jsonPath("$.data[0].type").value(TransactionType.EXPENSE.name()));
  }

  private String registerUserAndGetJwt() throws Exception {
    RegisterRequest registerRequest =
        RegisterRequest.builder()
            .email(EMAIL)
            .password(PASSWORD)
            .firstName("Vinay")
            .lastName("R")
            .build();
    mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));
    LoginRequest loginRequest =
        LoginRequest.builder().email("vinay@test.com").password("Vinay123").build();
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andReturn();
    String responseBody = mvcResult.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);

    return jsonNode.path("data").path("token").asText();
  }

  private ResultActions performAuthenticatedPost(String token, TransactionRequest request)
      throws Exception {
    return mockMvc.perform(
        post(TransactionIntegrationTest.TRANSACTIONS_CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
  }

  private TransactionRequest createTransactionRequest() {
    Category category = categoryRepository.findByName("Travel");
    return TransactionRequest.builder()
        .amount(new BigDecimal("150.62"))
        .type(TransactionType.EXPENSE)
        .categoryId(category.getId())
        .description("Uber auto to Railway Station")
        .transactionDate(TEST_TIME)
        .build();
  }

  private UUID createTransactionAndReturnId() throws Exception {
    TransactionRequest transactionRequest = createTransactionRequest();
    ResultActions resultActions = performAuthenticatedPost(jwtToken, transactionRequest);
    String responseBody = resultActions.andReturn().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    return UUID.fromString(jsonNode.path("data").path("id").asText());
  }

  private ResultActions performAuthenticatedGet(String token, String path) throws Exception {
    return mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
  }
}
