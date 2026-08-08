package com.vinay.moneymanager.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinay.moneymanager.budget.dto.request.CreateBudgetRequest;
import com.vinay.moneymanager.budget.dto.request.UpdateBudgetRequest;
import com.vinay.moneymanager.budget.dto.response.BudgetResponse;
import com.vinay.moneymanager.transaction.dto.request.TransactionRequest;
import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import com.vinay.moneymanager.transaction.repository.CategoryRepository;
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
class BudgetIntegrationTest {

  public static final String EMAIL = "vinay@test.com";
  public static final String BUDGETS_CONTEXT_PATH = "/api/budgets";
  private static final String PASSWORD = "Vinay123";
  private static final LocalDateTime TEST_TIME = LocalDateTime.of(2026, Month.AUGUST, 8, 12, 0);
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private CategoryRepository categoryRepository;
  private String jwtToken;
  private Category foodCategory;
  private Category travelCategory;
  private Category salaryCategory;
  private BudgetResponse travelBudgetResponse;

  @BeforeEach
  void setup() throws Exception {
    RegisterRequest registerRequest = new RegisterRequest(EMAIL, "Vinay", "R", PASSWORD);
    jwtToken = registerUserAndGetJwt(registerRequest);
    foodCategory = categoryRepository.findByName("Food");
    travelCategory = categoryRepository.findByName("Travel");
    salaryCategory = categoryRepository.findByName("Salary");
    CreateBudgetRequest travelBudgetRequest = getCreateBudgetRequest(travelCategory);
    travelBudgetResponse = createBudgetInDB(travelBudgetRequest, jwtToken);
  }

  private BudgetResponse createBudgetInDB(CreateBudgetRequest budgetRequest, String jwtToken)
      throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(
                post(BUDGETS_CONTEXT_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(budgetRequest))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
            .andExpect(status().isCreated())
            .andReturn();
    String responseBody = mvcResult.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    JsonNode dataNode = jsonNode.path("data");
    return objectMapper.treeToValue(dataNode, BudgetResponse.class);
  }

  // CREATE
  @Test
  void shouldCreateBudgetSuccessfully() throws Exception {
    CreateBudgetRequest budgetRequest = getCreateBudgetRequest(foodCategory);
    performAuthenticatedPost(budgetRequest, jwtToken)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Budget added successfully"))
        .andExpect(jsonPath("$.data.categoryName").value(foodCategory.getName()));
  }

  @Test
  void shouldReturnConflictWhenBudgetAlreadyExists() throws Exception {
    CreateBudgetRequest budgetRequest = getCreateBudgetRequest(travelCategory);
    performAuthenticatedPost(budgetRequest, jwtToken)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message").value("Budget already exists for this category and month"));
  }

  @Test
  void shouldReturnBadRequestWhenCategoryIsIncome() throws Exception {
    CreateBudgetRequest budgetRequest = getCreateBudgetRequest(salaryCategory);
    performAuthenticatedPost(budgetRequest, jwtToken)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message").value("Budgets can only be created for expense categories"));
  }

  @Test
  void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
    CreateBudgetRequest budgetRequest = getCreateBudgetRequest(foodCategory);
    budgetRequest.setAmount(BigDecimal.ZERO);
    performAuthenticatedPost(budgetRequest, jwtToken)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors[0].field").value("amount"))
        .andExpect(jsonPath("$.errors[0].message").value("must be greater than or equal to 0.01"));
  }

  // GET BY ID
  @Test
  void shouldReturnBudgetByIdSuccessfully() throws Exception {
    mockMvc
        .perform(
            get(BUDGETS_CONTEXT_PATH + "/" + travelBudgetResponse.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Budget retrieved successfully"))
        .andExpect(jsonPath("$.data.id").value(travelBudgetResponse.getId().toString()));
  }

  @Test
  void shouldReturnNotFoundWhenBudgetDoesNotExist() throws Exception {
    UUID nonExistentBudgetId = UUID.randomUUID();
    mockMvc
        .perform(
            get(BUDGETS_CONTEXT_PATH + "/" + nonExistentBudgetId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Budget not found"));
  }

  @Test
  void shouldReturnNotFoundWhenBudgetBelongsToAnotherUser() throws Exception {
    RegisterRequest registerRequest =
        new RegisterRequest("User2@test.com", "User2", "User2", "Password987");
    String anotherUserJwt = registerUserAndGetJwt(registerRequest);

    mockMvc
        .perform(
            get(BUDGETS_CONTEXT_PATH + "/" + travelBudgetResponse.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + anotherUserJwt))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Budget not found"));
  }

  // GET ALL
  @Test
  void shouldReturnAllBudgetsForAuthenticatedUser() throws Exception {
    CreateBudgetRequest foodBudgetRequest = getCreateBudgetRequest(foodCategory);
    createBudgetInDB(foodBudgetRequest, jwtToken);
    mockMvc
        .perform(get(BUDGETS_CONTEXT_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Budgets retrieved successfully"))
        .andExpect(jsonPath("$.data").isNotEmpty())
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  @Test
  void shouldReturnEmptyListWhenUserHasNoBudgets() throws Exception {
    RegisterRequest registerRequest =
        new RegisterRequest("User2@test.com", "User2", "User2", "Password987");
    String token = registerUserAndGetJwt(registerRequest);
    mockMvc
        .perform(get(BUDGETS_CONTEXT_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Budgets retrieved successfully"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  // GET BY MONTH
  @Test
  void shouldReturnBudgetsForSpecifiedMonth() throws Exception {
    CreateBudgetRequest foodBudgetRequest = getCreateBudgetRequest(foodCategory);
    foodBudgetRequest.setMonth(Month.SEPTEMBER);
    createBudgetInDB(foodBudgetRequest, jwtToken);
    mockMvc
        .perform(
            get(BUDGETS_CONTEXT_PATH
                    + "/monthly?month="
                    + foodBudgetRequest.getMonth()
                    + "&year="
                    + foodBudgetRequest.getYear())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Monthly budgets retrieved successfully"))
        .andExpect(jsonPath("$.data").isNotEmpty())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].month").value("SEPTEMBER"));
  }

  @Test
  void shouldReturnEmptyListWhenNoBudgetsExistForMonth() throws Exception {
    mockMvc
        .perform(
            get(BUDGETS_CONTEXT_PATH + "/monthly?month=" + Month.SEPTEMBER + "&year=" + 2026)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Monthly budgets retrieved successfully"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  //   UPDATE
  @Test
  void shouldUpdateBudgetSuccessfully() throws Exception {
    UpdateBudgetRequest updateBudgetRequest = new UpdateBudgetRequest(BigDecimal.valueOf(13000));
    mockMvc
        .perform(
            put(BUDGETS_CONTEXT_PATH + "/" + travelBudgetResponse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBudgetRequest))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Budget updated successfully"))
        .andExpect(jsonPath("$.data.amount").value(13000));
  }

  @Test
  void shouldReturnNotFoundWhenUpdatingNonExistingBudget() throws Exception {
    UpdateBudgetRequest updateBudgetRequest = new UpdateBudgetRequest(BigDecimal.valueOf(13000));
    UUID nonExistentBudgetId = UUID.randomUUID();
    mockMvc
        .perform(
            put(BUDGETS_CONTEXT_PATH + "/" + nonExistentBudgetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBudgetRequest))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Budget not found"));
  }

  @Test
  void shouldReturnBadRequestWhenUpdateRequestIsInvalid() throws Exception {
    UpdateBudgetRequest updateBudgetRequest = new UpdateBudgetRequest(BigDecimal.valueOf(-120));
    mockMvc
        .perform(
            put(BUDGETS_CONTEXT_PATH + "/" + travelBudgetResponse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBudgetRequest))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors[0].field").value("amount"))
        .andExpect(jsonPath("$.errors[0].message").value("must be greater than or equal to 0.01"));
  }

  @Test
  void shouldReturnNotFoundWhenUpdatingAnotherUsersBudget() throws Exception {
    RegisterRequest registerRequest =
        new RegisterRequest("User2@test.com", "User2", "User2", "Password987");
    String anotherUserJwt = registerUserAndGetJwt(registerRequest);
    UpdateBudgetRequest updateBudgetRequest = new UpdateBudgetRequest(BigDecimal.valueOf(13000));
    mockMvc
        .perform(
            put(BUDGETS_CONTEXT_PATH + "/" + travelBudgetResponse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBudgetRequest))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + anotherUserJwt))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Budget not found"));
  }

  //   DELETE
  @Test
  void shouldDeleteBudgetSuccessfully() throws Exception {
    mockMvc
        .perform(
            delete(BUDGETS_CONTEXT_PATH + "/" + travelBudgetResponse.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Budget deleted successfully"));
  }

  @Test
  void shouldReturnNotFoundWhenDeletingNonExistingBudget() throws Exception {
    UUID nonExistentBudgetId = UUID.randomUUID();
    mockMvc
        .perform(
            delete(BUDGETS_CONTEXT_PATH + "/" + nonExistentBudgetId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Budget not found"));
  }

  @Test
  void shouldReturnNotFoundWhenDeletingAnotherUsersBudget() throws Exception {
    RegisterRequest registerRequest =
        new RegisterRequest("User2@test.com", "User2", "User2", "Password987");
    String anotherUserJwt = registerUserAndGetJwt(registerRequest);
    mockMvc
        .perform(
            delete(BUDGETS_CONTEXT_PATH + "/" + travelBudgetResponse.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + anotherUserJwt))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Budget not found"));
  }

  //   SECURITY
  @Test
  void shouldReturnUnauthorizedWhenJwtIsMissing() throws Exception {
    mockMvc
        .perform(get(BUDGETS_CONTEXT_PATH))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }

  @Test
  void shouldCalculateSpentAndRemainingAmountCorrectly() throws Exception {

    TransactionRequest transactionRequest = createTransactionRequest();

    mockMvc
        .perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get(BUDGETS_CONTEXT_PATH + "/" + travelBudgetResponse.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.spentAmount").value(150.62))
        .andExpect(jsonPath("$.data.remainingAmount").value(9849.38));
  }

  private String registerUserAndGetJwt(RegisterRequest registerRequest) throws Exception {
    mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));
    LoginRequest loginRequest =
        LoginRequest.builder()
            .email(registerRequest.getEmail())
            .password(registerRequest.getPassword())
            .build();
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

  private CreateBudgetRequest getCreateBudgetRequest(Category category) {
    return CreateBudgetRequest.builder()
        .categoryId(category.getId())
        .amount(BigDecimal.valueOf(10000))
        .month(Month.AUGUST)
        .year(2026)
        .build();
  }

  private ResultActions performAuthenticatedPost(Object payload, String jwtToken) throws Exception {
    return mockMvc.perform(
        post(BudgetIntegrationTest.BUDGETS_CONTEXT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken));
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
}
