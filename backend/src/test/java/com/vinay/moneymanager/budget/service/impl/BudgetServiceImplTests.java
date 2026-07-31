package com.vinay.moneymanager.budget.service.impl;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vinay.moneymanager.budget.dto.request.CreateBudgetRequest;
import com.vinay.moneymanager.budget.dto.response.BudgetResponse;
import com.vinay.moneymanager.budget.entity.Budget;
import com.vinay.moneymanager.budget.repository.BudgetRepository;
import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import com.vinay.moneymanager.transaction.repository.CategoryRepository;
import com.vinay.moneymanager.transaction.repository.TransactionRepository;
import com.vinay.moneymanager.user.entity.User;
import com.vinay.moneymanager.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTests {

  @Mock private BudgetRepository budgetRepository;
  @Mock private UserRepository userRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private TransactionRepository transactionRepository;
  @InjectMocks private BudgetServiceImpl budgetService;
  @Captor private ArgumentCaptor<Budget> budgetCaptor;

  private User user;
  private Category category;

  @BeforeEach
  void setUp() {
    user = getUser();
    category = getCategory();
  }

  private Category getCategory() {
    return Category.builder().id(1).name("Food").transactionType(TransactionType.EXPENSE).build();
  }

  @Test
  void shouldCreateBudgetSuccessfully() {
    CreateBudgetRequest request = createBudgetRequest(category);
    Budget savedBudget = getSavedBudget(request, category, user);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(category));
    when(budgetRepository.findByUserAndCategoryAndMonthAndYear(
            user, category, request.getMonth(), request.getYear()))
        .thenReturn(Optional.empty());
    when(transactionRepository.sumExpenseByUserAndCategoryAndMonthAndYear(
            any(), any(), anyInt(), anyInt()))
        .thenReturn(ZERO);
    when(budgetRepository.save(any(Budget.class))).thenReturn(savedBudget);

    BudgetResponse budgetResponse = budgetService.createBudget(request, user.getEmail());

    verify(budgetRepository).save(budgetCaptor.capture());
    Budget capturedBudget = budgetCaptor.getValue();

    assertEquals(user, capturedBudget.getUser());
    assertEquals(category, capturedBudget.getCategory());
    assertEquals(request.getMonth(), capturedBudget.getMonth());
    assertEquals(request.getYear(), capturedBudget.getYear());
    assertEquals(request.getAmount(), capturedBudget.getAmount());
    assertNotNull(budgetResponse);
    assertEquals(budgetResponse.getAmount(), capturedBudget.getAmount());
    assertEquals(budgetResponse.getRemainingAmount(), capturedBudget.getAmount());
    assertEquals(ZERO, budgetResponse.getSpentAmount());
  }

  @Test
  void shouldReturnBudgetSuccessfully() {
    CreateBudgetRequest request = createBudgetRequest(category);
    Budget savedBudget = getSavedBudget(request, category, user);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(budgetRepository.findByIdAndUser(any(UUID.class), any(User.class)))
        .thenReturn(Optional.of(savedBudget));
    when(transactionRepository.sumExpenseByUserAndCategoryAndMonthAndYear(
            any(), any(), anyInt(), anyInt()))
        .thenReturn(ZERO);
    BudgetResponse budgetResponse =
        budgetService.getBudgetById(savedBudget.getId(), user.getEmail());
    verify(budgetRepository).findByIdAndUser(any(UUID.class), any(User.class));

    assertEquals(savedBudget.getAmount(), budgetResponse.getAmount());
    assertEquals(savedBudget.getId(), budgetResponse.getId());
    assertEquals(savedBudget.getMonth(), budgetResponse.getMonth());
    assertEquals(savedBudget.getYear(), budgetResponse.getYear());
  }

  private Budget getSavedBudget(CreateBudgetRequest request, Category category, User user) {
    LocalDateTime createdAt = LocalDateTime.of(2026, Month.JULY, 30, 17, 53, 0);
    return Budget.builder()
        .id(UUID.randomUUID())
        .amount(request.getAmount())
        .month(request.getMonth())
        .year(request.getYear())
        .category(category)
        .user(user)
        .createdAt(createdAt)
        .updatedAt(createdAt)
        .build();
  }

  private CreateBudgetRequest createBudgetRequest(Category category) {
    return CreateBudgetRequest.builder()
        .categoryId(category.getId())
        .month(Month.JULY)
        .year(2026)
        .amount(BigDecimal.valueOf(10000))
        .build();
  }

  private User getUser() {
    return User.builder()
        .id(UUID.randomUUID())
        .email("vinay@test.com")
        .password("Password@123")
        .firstName("Vinay")
        .lastName("R")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .enabled(true)
        .build();
  }
}
