package com.vinay.moneymanager.budget.service.impl;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.vinay.moneymanager.budget.dto.request.CreateBudgetRequest;
import com.vinay.moneymanager.budget.dto.request.UpdateBudgetRequest;
import com.vinay.moneymanager.budget.dto.response.BudgetResponse;
import com.vinay.moneymanager.budget.entity.Budget;
import com.vinay.moneymanager.budget.repository.BudgetRepository;
import com.vinay.moneymanager.common.exception.DuplicateResourceException;
import com.vinay.moneymanager.common.exception.InvalidRequestException;
import com.vinay.moneymanager.common.exception.ResourceNotFoundException;
import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import com.vinay.moneymanager.transaction.repository.CategoryRepository;
import com.vinay.moneymanager.transaction.repository.TransactionRepository;
import com.vinay.moneymanager.user.entity.User;
import com.vinay.moneymanager.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
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

  public static final String AUTHENTICATED_USER_NOT_FOUND = "Authenticated user not found";
  @Mock private BudgetRepository budgetRepository;
  @Mock private UserRepository userRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private TransactionRepository transactionRepository;
  @InjectMocks private BudgetServiceImpl budgetService;
  @Captor private ArgumentCaptor<Budget> budgetCaptor;

  private User user;
  private Category category;
  private Budget budget;

  @BeforeEach
  void setUp() {
    user = getUser();
    category = getCategory();
    budget = getSavedBudget(createBudgetRequest(category), category, user);
  }

  // CREATE
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
  void shouldThrowExceptionWhenUserNotFound() {
    CreateBudgetRequest request = createBudgetRequest(category);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrowsExactly(
            ResourceNotFoundException.class,
            () -> budgetService.createBudget(request, user.getEmail()));

    assertEquals(AUTHENTICATED_USER_NOT_FOUND, exception.getMessage());
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void shouldThrowExceptionWhenCategoryNotFound() {
    CreateBudgetRequest request = createBudgetRequest(category);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrowsExactly(
            ResourceNotFoundException.class,
            () -> budgetService.createBudget(request, user.getEmail()));

    assertEquals("Category not found", exception.getMessage());
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void shouldThrowExceptionWhenCategoryIsIncome() {
    CreateBudgetRequest request = createBudgetRequest(category);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    category.setTransactionType(TransactionType.INCOME);
    when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(category));

    InvalidRequestException exception =
        assertThrowsExactly(
            InvalidRequestException.class,
            () -> budgetService.createBudget(request, user.getEmail()));

    assertEquals("Budgets can only be created for expense categories", exception.getMessage());
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void shouldThrowExceptionWhenBudgetAlreadyExists() {
    CreateBudgetRequest request = createBudgetRequest(category);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(category));
    when(budgetRepository.findByUserAndCategoryAndMonthAndYear(
            user, category, request.getMonth(), request.getYear()))
        .thenReturn(Optional.of(budget));

    DuplicateResourceException exception =
        assertThrowsExactly(
            DuplicateResourceException.class,
            () -> budgetService.createBudget(request, user.getEmail()));
    assertEquals("Budget already exists for this category and month", exception.getMessage());
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  // GET BY ID
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

  @Test
  void shouldThrowExceptionWhenBudgetNotFound() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(budgetRepository.findByIdAndUser(any(UUID.class), any(User.class)))
        .thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrowsExactly(
            ResourceNotFoundException.class,
            () -> budgetService.getBudgetById(budget.getId(), user.getEmail()));
    assertEquals("Budget not found", exception.getMessage());
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void shouldThrowExceptionWhenAuthenticatedUserNotFound() {

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    ResourceNotFoundException exception =
        assertThrowsExactly(
            ResourceNotFoundException.class,
            () -> budgetService.getBudgetById(budget.getId(), user.getEmail()));
    assertEquals(AUTHENTICATED_USER_NOT_FOUND, exception.getMessage());
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  //  GET ALL
  @Test
  void shouldReturnAllBudgetsForUser() {
    List<Budget> budgets = createBudgetList(user);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(budgetRepository.findByUser(user)).thenReturn(budgets);
    when(transactionRepository.sumExpenseByUserAndCategoryAndMonthAndYear(
            any(), any(), anyInt(), anyInt()))
        .thenReturn(ZERO);
    List<BudgetResponse> allBudgets = budgetService.getAllBudgets(user.getEmail());
    verify(budgetRepository).findByUser(user);
    assertEquals(3, allBudgets.size());
    assertEquals(Month.JULY, allBudgets.getFirst().getMonth());
    assertEquals(2026, allBudgets.getFirst().getYear());
  }

  @Test
  void shouldReturnEmptyListWhenUserHasNoBudgets() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(budgetRepository.findByUser(user)).thenReturn(Collections.emptyList());
    List<BudgetResponse> allBudgets = budgetService.getAllBudgets(user.getEmail());
    verify(budgetRepository).findByUser(user);
    assertEquals(0, allBudgets.size());
  }

  @Test
  void shouldThrowExceptionWhenUserNotFoundWhileGettingBudgets() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    ResourceNotFoundException exception =
        assertThrowsExactly(
            ResourceNotFoundException.class, () -> budgetService.getAllBudgets(user.getEmail()));
    assertEquals(AUTHENTICATED_USER_NOT_FOUND, exception.getMessage());
    verify(budgetRepository, never()).findByIdAndUser(any(UUID.class), any(User.class));
  }

  //  GET BY MONTH
  @Test
  void shouldReturnBudgetsForMonthAndYear() {
    List<Budget> budgets = createBudgetList(user);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(budgetRepository.findByUserAndMonthAndYear(user, Month.JULY, 2026)).thenReturn(budgets);
    when(transactionRepository.sumExpenseByUserAndCategoryAndMonthAndYear(
            any(), any(), anyInt(), anyInt()))
        .thenReturn(ZERO);
    List<BudgetResponse> allBudgets =
        budgetService.getBudgetsByMonth(Month.JULY, 2026, user.getEmail());
    verify(budgetRepository).findByUserAndMonthAndYear(user, Month.JULY, 2026);
    assertEquals(3, allBudgets.size());
    assertEquals(Month.JULY, allBudgets.getFirst().getMonth());
    assertEquals(2026, allBudgets.getFirst().getYear());
  }

  @Test
  void shouldReturnEmptyListWhenNoBudgetsExistForMonth() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(budgetRepository.findByUserAndMonthAndYear(any(), any(), anyInt()))
        .thenReturn(Collections.emptyList());
    List<BudgetResponse> allBudgets =
        budgetService.getBudgetsByMonth(Month.JULY, 2026, user.getEmail());
    assertEquals(0, allBudgets.size());
    verify(budgetRepository).findByUserAndMonthAndYear(any(), any(), anyInt());
  }

  @Test
  void shouldThrowExceptionWhenUserNotFoundWhileGettingBudgetsByMonth() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    ResourceNotFoundException exception =
        assertThrowsExactly(
            ResourceNotFoundException.class,
            () -> budgetService.getBudgetsByMonth(Month.JULY, 2026, user.getEmail()));
    assertEquals(AUTHENTICATED_USER_NOT_FOUND, exception.getMessage());
    verify(budgetRepository, never()).findByIdAndUser(any(UUID.class), any(User.class));
  }

  //  UPDATE
  @Test
  void shouldUpdateBudgetSuccessfully() {
    UpdateBudgetRequest updateBudgetRequest = new UpdateBudgetRequest(BigDecimal.valueOf(13000));
    Budget upddatedBudget = getSavedBudget(createBudgetRequest(category), category, user);
    upddatedBudget.setAmount(updateBudgetRequest.getAmount());
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(budgetRepository.findByIdAndUser(any(UUID.class), any(User.class)))
        .thenReturn(Optional.of(budget));
    when(budgetRepository.save(budget)).thenReturn(upddatedBudget);
    BudgetResponse budgetResponse =
        budgetService.updateBudget(budget.getId(), updateBudgetRequest, user.getEmail());
    assertEquals(BigDecimal.valueOf(13000), budgetResponse.getAmount());
    verify(budgetRepository).save(budgetCaptor.capture());
    Budget captured = budgetCaptor.getValue();
    assertEquals(BigDecimal.valueOf(13000), captured.getAmount());
    verify(budgetRepository).findByIdAndUser(any(UUID.class), any(User.class));
    verify(budgetRepository).save(budget);
  }

  @Test
  void shouldThrowExceptionWhenBudgetNotFoundWhileUpdating() {
    UpdateBudgetRequest updateBudgetRequest = new UpdateBudgetRequest(BigDecimal.valueOf(13000));
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(budgetRepository.findByIdAndUser(any(UUID.class), any(User.class)))
        .thenReturn(Optional.empty());
    ResourceNotFoundException exception =
        assertThrowsExactly(
            ResourceNotFoundException.class,
            () ->
                budgetService.updateBudget(
                    UUID.randomUUID(), updateBudgetRequest, user.getEmail()));
    assertEquals("Budget not found", exception.getMessage());
    verify(budgetRepository).findByIdAndUser(any(UUID.class), any(User.class));
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void shouldThrowExceptionWhenAuthenticatedUserNotFoundWhileUpdating() {
    UpdateBudgetRequest updateBudgetRequest = new UpdateBudgetRequest(BigDecimal.valueOf(13000));
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    ResourceNotFoundException exception =
        assertThrowsExactly(
            ResourceNotFoundException.class,
            () ->
                budgetService.updateBudget(
                    UUID.randomUUID(), updateBudgetRequest, user.getEmail()));
    assertEquals(AUTHENTICATED_USER_NOT_FOUND, exception.getMessage());
    verify(budgetRepository, never()).findByIdAndUser(any(UUID.class), any(User.class));
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  //  DELETE
  @Test
  void shouldDeleteBudgetSuccessfully() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(budgetRepository.findByIdAndUser(any(UUID.class), any(User.class)))
        .thenReturn(Optional.of(budget));
    budgetService.deleteBudget(budget.getId(), user.getEmail());

    verify(budgetRepository).findByIdAndUser(any(UUID.class), any(User.class));
    verify(budgetRepository).delete(budget);
  }

  @Test
  void shouldThrowExceptionWhenBudgetNotFoundWhileDeleting() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(budgetRepository.findByIdAndUser(any(UUID.class), any(User.class)))
        .thenReturn(Optional.empty());
    ResourceNotFoundException exception =
        assertThrowsExactly(
            ResourceNotFoundException.class,
            () -> budgetService.deleteBudget(UUID.randomUUID(), user.getEmail()));
    assertEquals("Budget not found", exception.getMessage());
    verify(budgetRepository).findByIdAndUser(any(UUID.class), any(User.class));
    verify(budgetRepository, never()).delete(any(Budget.class));
  }

  @Test
  void shouldThrowExceptionWhenAuthenticatedUserNotFoundWhileDeleting() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    ResourceNotFoundException exception =
        assertThrowsExactly(
            ResourceNotFoundException.class,
            () -> budgetService.deleteBudget(UUID.randomUUID(), user.getEmail()));
    assertEquals(AUTHENTICATED_USER_NOT_FOUND, exception.getMessage());
    verify(budgetRepository, never()).findByIdAndUser(any(UUID.class), any(User.class));
    verify(budgetRepository, never()).delete(any(Budget.class));
  }

  // Helper Methods
  private Category getCategory() {
    return Category.builder().id(1).name("Food").transactionType(TransactionType.EXPENSE).build();
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

  private List<Budget> createBudgetList(User user) {
    List<Budget> budgets = new ArrayList<>();
    Budget budget1 = getSavedBudget(createBudgetRequest(category), category, user);
    Budget budget2 =
        Budget.builder()
            .id(UUID.randomUUID())
            .user(user)
            .category(new Category(2, "Travel", "description", TransactionType.EXPENSE))
            .month(budget1.getMonth())
            .year(budget1.getYear())
            .amount(BigDecimal.valueOf(30000))
            .build();
    Budget budget3 =
        Budget.builder()
            .id(UUID.randomUUID())
            .user(user)
            .category(new Category(2, "Groceries", "description", TransactionType.EXPENSE))
            .month(budget1.getMonth())
            .year(budget1.getYear())
            .amount(BigDecimal.valueOf(20000))
            .build();
    budgets.add(budget1);
    budgets.add(budget2);
    budgets.add(budget3);
    return budgets;
  }
}
