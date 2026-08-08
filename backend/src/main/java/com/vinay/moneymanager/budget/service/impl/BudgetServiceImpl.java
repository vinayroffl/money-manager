package com.vinay.moneymanager.budget.service.impl;

import com.vinay.moneymanager.budget.dto.request.CreateBudgetRequest;
import com.vinay.moneymanager.budget.dto.request.UpdateBudgetRequest;
import com.vinay.moneymanager.budget.dto.response.BudgetResponse;
import com.vinay.moneymanager.budget.entity.Budget;
import com.vinay.moneymanager.budget.repository.BudgetRepository;
import com.vinay.moneymanager.budget.service.BudgetService;
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
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

  public static final String BUDGET_NOT_FOUND = "Budget not found";
  private final BudgetRepository budgetRepository;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final TransactionRepository transactionRepository;

  @Override
  public BudgetResponse createBudget(CreateBudgetRequest request, String userEmail) {
    User user = getAuthenticatedUser(userEmail);
    Category category = getCategory(request.getCategoryId());
    validateExpenseCategory(category);
    validateDuplicateBudget(user, category, request);
    Budget budget = mapToBudget(request, user, category);
    Budget savedBudget = budgetRepository.save(budget);
    return mapToBudgetResponse(savedBudget, user);
  }

  @Override
  public BudgetResponse getBudgetById(UUID budgetId, String userEmail) {
    User user = getAuthenticatedUser(userEmail);
    Budget budget =
        budgetRepository
            .findByIdAndUser(budgetId, user)
            .orElseThrow(() -> new ResourceNotFoundException(BUDGET_NOT_FOUND));
    return mapToBudgetResponse(budget, user);
  }

  @Override
  public List<BudgetResponse> getAllBudgets(String userEmail) {
    User user = getAuthenticatedUser(userEmail);
    List<Budget> budgets = budgetRepository.findByUser(user);
    return mapToBudgetResponses(budgets, user);
  }

  @Override
  public List<BudgetResponse> getBudgetsByMonth(Month month, Integer year, String userEmail) {
    User user = getAuthenticatedUser(userEmail);
    List<Budget> budgets = budgetRepository.findByUserAndMonthAndYear(user, month, year);
    return mapToBudgetResponses(budgets, user);
  }

  @Override
  public BudgetResponse updateBudget(UUID budgetId, UpdateBudgetRequest request, String userEmail) {
    User user = getAuthenticatedUser(userEmail);
    Budget budget =
        budgetRepository
            .findByIdAndUser(budgetId, user)
            .orElseThrow(() -> new ResourceNotFoundException(BUDGET_NOT_FOUND));
    budget.setAmount(request.getAmount());
    Budget updatedBudget = budgetRepository.save(budget);
    return mapToBudgetResponse(updatedBudget, user);
  }

  @Override
  public void deleteBudget(UUID budgetId, String userEmail) {
    User user = getAuthenticatedUser(userEmail);
    Budget budget =
        budgetRepository
            .findByIdAndUser(budgetId, user)
            .orElseThrow(() -> new ResourceNotFoundException(BUDGET_NOT_FOUND));
    budgetRepository.delete(budget);
  }

  private void validateExpenseCategory(Category category) {
    if (category.getTransactionType() != TransactionType.EXPENSE) {
      throw new InvalidRequestException("Budgets can only be created for expense categories");
    }
  }

  private void validateDuplicateBudget(User user, Category category, CreateBudgetRequest request) {
    budgetRepository
        .findByUserAndCategoryAndMonthAndYear(user, category, request.getMonth(), request.getYear())
        .ifPresent(
            budget -> {
              throw new DuplicateResourceException(
                  "Budget already exists for this category and month");
            });
  }

  private BudgetResponse mapToBudgetResponse(Budget budget, User user) {
    BigDecimal spentAmount = fetchSpentAmount(budget, user);
    BigDecimal remainingAmount = budget.getAmount().subtract(spentAmount);
    return BudgetResponse.builder()
        .id(budget.getId())
        .categoryName(budget.getCategory().getName())
        .amount(budget.getAmount())
        .month(budget.getMonth())
        .year(budget.getYear())
        .remainingAmount(remainingAmount)
        .spentAmount(spentAmount)
        .build();
  }

  private List<BudgetResponse> mapToBudgetResponses(List<Budget> budgets, User user) {
    return budgets.stream().map(budget -> mapToBudgetResponse(budget, user)).toList();
  }

  private BigDecimal fetchSpentAmount(Budget budget, User user) {
    return Optional.ofNullable(
            transactionRepository.sumExpenseByUserAndCategoryAndMonthAndYear(
                user, budget.getCategory(), budget.getMonth().getValue(), budget.getYear()))
        .orElse(BigDecimal.ZERO);
  }

  private Budget mapToBudget(CreateBudgetRequest request, User user, Category category) {
    return Budget.builder()
        .amount(request.getAmount())
        .month(request.getMonth())
        .year(request.getYear())
        .category(category)
        .user(user)
        .build();
  }

  private Category getCategory(Integer categoryId) {
    return categoryRepository
        .findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
  }

  private User getAuthenticatedUser(String userEmail) {
    return userRepository
        .findByEmail(userEmail)
        .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
  }
}
