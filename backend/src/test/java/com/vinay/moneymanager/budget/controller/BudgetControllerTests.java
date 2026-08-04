package com.vinay.moneymanager.budget.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinay.moneymanager.budget.dto.request.CreateBudgetRequest;
import com.vinay.moneymanager.budget.dto.request.UpdateBudgetRequest;
import com.vinay.moneymanager.budget.dto.response.BudgetResponse;
import com.vinay.moneymanager.budget.service.BudgetService;
import com.vinay.moneymanager.common.exception.GlobalExceptionHandler;
import com.vinay.moneymanager.common.exception.ResourceNotFoundException;
import com.vinay.moneymanager.config.SecurityConfig;
import com.vinay.moneymanager.security.entrypoint.JwtAuthenticationEntryPoint;
import com.vinay.moneymanager.security.jwt.JwtService;
import com.vinay.moneymanager.security.service.CustomUserDetailsService;
import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Month;
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

@WebMvcTest(BudgetController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class BudgetControllerTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private BudgetService budgetService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  @MockitoBean private CustomUserDetailsService customUserDetailsService;

  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldCreateBudgetSuccessfully() throws Exception {
    Category category = createCategory(1, "Travel");
    CreateBudgetRequest createBudgetRequest = getCreateBudgetRequest(category);
    BudgetResponse budgetResponse = getBudgetResponse(createBudgetRequest, category);
    when(budgetService.createBudget(any(CreateBudgetRequest.class), anyString()))
        .thenReturn(budgetResponse);

    mockMvc
        .perform(
            post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBudgetRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Budget added successfully"))
        .andExpect(jsonPath("$.data.amount").value(createBudgetRequest.getAmount()))
        .andExpect(jsonPath("$.data.categoryName").value("Travel"))
        .andExpect(jsonPath("$.data.spentAmount").value(1000))
        .andExpect(jsonPath("$.data.remainingAmount").value(9000));
  }

  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldReturnBadRequestWhenCreateBudgetRequestIsInvalid() throws Exception {
    Category category = createCategory(1, "Travel");
    CreateBudgetRequest createBudgetRequest = getCreateBudgetRequest(category);
    createBudgetRequest.setAmount(BigDecimal.valueOf(-1));
    mockMvc
        .perform(
            post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBudgetRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors[0].message").value("must be greater than or equal to 0.01"));
  }

  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldGetBudgetByIdSuccessfully() throws Exception {
    Category category = createCategory(1, "Travel");
    CreateBudgetRequest createBudgetRequest = getCreateBudgetRequest(category);
    BudgetResponse budgetResponse = getBudgetResponse(createBudgetRequest, category);
    when(budgetService.getBudgetById(any(UUID.class), anyString())).thenReturn(budgetResponse);
    mockMvc
        .perform(get("/api/budgets/" + budgetResponse.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Budget retrieved successfully"))
        .andExpect(jsonPath("$.data.amount").value(budgetResponse.getAmount()))
        .andExpect(jsonPath("$.data.categoryName").value("Travel"))
        .andExpect(jsonPath("$.data.spentAmount").value(1000));
    verify(budgetService).getBudgetById(budgetResponse.getId(), "vinay@test.com");
  }

  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldReturnNotFoundWhenBudgetDoesNotExist() throws Exception {
    UUID nonExistentBudgetId = UUID.randomUUID();
    when(budgetService.getBudgetById(any(UUID.class), anyString()))
        .thenThrow(
            new ResourceNotFoundException("Budget not found with id: " + nonExistentBudgetId));

    mockMvc
        .perform(get("/api/budgets/" + nonExistentBudgetId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
    verify(budgetService).getBudgetById(nonExistentBudgetId, "vinay@test.com");
  }

  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldGetAllBudgetsSuccessfully() throws Exception {
    List<BudgetResponse> budgetResponses = createListOfBudgetResponses();
    when(budgetService.getAllBudgets(anyString())).thenReturn(budgetResponses);
    mockMvc
        .perform(get("/api/budgets"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Budgets retrieved successfully"))
        .andExpect(jsonPath("$.data[0].amount").value(budgetResponses.getFirst().getAmount()));
    verify(budgetService).getAllBudgets(anyString());
  }

  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldGetBudgetsByMonthSuccessfully() throws Exception {
    List<BudgetResponse> septemberBudgets =
        createListOfBudgetResponses().stream()
            .filter(budgetResponse -> budgetResponse.getMonth().equals(Month.SEPTEMBER))
            .toList();
    when(budgetService.getBudgetsByMonth(Month.SEPTEMBER, 2026, "vinay@test.com"))
        .thenReturn(septemberBudgets);
    mockMvc
        .perform(get("/api/budgets/monthly").param("month", "SEPTEMBER").param("year", "2026"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Monthly budgets retrieved successfully"))
        .andExpect(jsonPath("$.data[0].amount").value(septemberBudgets.getFirst().getAmount()))
        .andExpect(jsonPath("$.data[0].month").value(Month.SEPTEMBER.toString()));
    verify(budgetService).getBudgetsByMonth(Month.SEPTEMBER, 2026, "vinay@test.com");
  }

  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldUpdateBudgetSuccessfully() throws Exception {
    Category travel = createCategory(1, "Travel");
    CreateBudgetRequest request = getCreateBudgetRequest(travel);
    BudgetResponse budgetResponse = getBudgetResponse(request, travel);
    UpdateBudgetRequest updateBudgetRequest = new UpdateBudgetRequest(BigDecimal.valueOf(20000));
    budgetResponse.setAmount(updateBudgetRequest.getAmount());
    when(budgetService.updateBudget(any(UUID.class), any(UpdateBudgetRequest.class), anyString()))
        .thenReturn(budgetResponse);

    mockMvc
        .perform(
            put("/api/budgets/" + budgetResponse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBudgetRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Budget updated successfully"))
        .andExpect(jsonPath("$.data.amount").value(budgetResponse.getAmount()));
    verify(budgetService)
        .updateBudget(any(UUID.class), any(UpdateBudgetRequest.class), anyString());
  }

  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldReturnBadRequestWhenUpdateBudgetRequestIsInvalid() throws Exception {
    Category travel = createCategory(1, "Travel");
    CreateBudgetRequest request = getCreateBudgetRequest(travel);
    BudgetResponse budgetResponse = getBudgetResponse(request, travel);
    UpdateBudgetRequest updateBudgetRequest = new UpdateBudgetRequest(BigDecimal.valueOf(20000));
    updateBudgetRequest.setAmount(BigDecimal.valueOf(-1));
    when(budgetService.updateBudget(any(UUID.class), any(UpdateBudgetRequest.class), anyString()))
        .thenThrow(new ResourceNotFoundException("Budget not found"));
    mockMvc
        .perform(
            put("/api/budgets/" + budgetResponse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBudgetRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors[0].message").value("must be greater than or equal to 0.01"));
    verify(budgetService, never())
        .updateBudget(any(UUID.class), any(UpdateBudgetRequest.class), anyString());
  }

  @Test
  @WithMockUser(username = "vinay@test.com")
  void shouldDeleteBudgetSuccessfully() throws Exception {
    UUID budgetId = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/budgets/" + budgetId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Budget deleted successfully"));
    verify(budgetService).deleteBudget(budgetId, "vinay@test.com");
  }

  private List<BudgetResponse> createListOfBudgetResponses() {
    List<BudgetResponse> budgetResponses = new ArrayList<>();
    Category travel = createCategory(1, "Travel");
    Category food = createCategory(2, "Food");
    Category groceries = createCategory(3, "Groceries");
    CreateBudgetRequest createBudgetRequest = getCreateBudgetRequest(travel);
    budgetResponses.add(getBudgetResponse(createBudgetRequest, travel));
    createBudgetRequest.setMonth(Month.SEPTEMBER);
    budgetResponses.add(getBudgetResponse(createBudgetRequest, food));
    budgetResponses.add(getBudgetResponse(createBudgetRequest, groceries));
    return budgetResponses;
  }

  private CreateBudgetRequest getCreateBudgetRequest(Category category) {
    return CreateBudgetRequest.builder()
        .categoryId(category.getId())
        .amount(BigDecimal.valueOf(10000))
        .month(Month.AUGUST)
        .year(2026)
        .build();
  }

  private Category createCategory(int id, String name) {
    return new Category(id, name, name + " expenses", TransactionType.EXPENSE);
  }

  private BudgetResponse getBudgetResponse(CreateBudgetRequest request, Category category) {
    return BudgetResponse.builder()
        .id(UUID.randomUUID())
        .categoryName(category.getName())
        .amount(request.getAmount())
        .month(request.getMonth())
        .year(request.getYear())
        .spentAmount(BigDecimal.valueOf(1000))
        .remainingAmount(BigDecimal.valueOf(9000))
        .build();
  }
}
