package com.vinay.moneymanager.budget.service.impl;

import com.vinay.moneymanager.budget.dto.request.CreateBudgetRequest;
import com.vinay.moneymanager.budget.dto.request.UpdateBudgetRequest;
import com.vinay.moneymanager.budget.dto.response.BudgetResponse;
import com.vinay.moneymanager.budget.entity.Budget;
import com.vinay.moneymanager.budget.repository.BudgetRepository;
import com.vinay.moneymanager.budget.service.BudgetService;
import com.vinay.moneymanager.common.exception.DuplicateResourceException;
import com.vinay.moneymanager.common.exception.FeatureNotImplementedException;
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
import java.util.UUID;
import lombok.RequiredArgsConstructor;

// @Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

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

    return mapToBudgetResponse(user, savedBudget);
  }

  @Override
  public BudgetResponse getBudgetById(UUID budgetId, String userEmail) {
    throw new FeatureNotImplementedException("Feature not Implemented");
  }

  @Override
  public List<BudgetResponse> getAllBudgets(String userEmail) {
    return List.of();
  }

  @Override
  public List<BudgetResponse> getBudgetsByMonth(Month month, Integer year, String userEmail) {
    return List.of();
  }

  @Override
  public BudgetResponse updateBudget(UUID budgetId, UpdateBudgetRequest request, String userEmail) {
    throw new FeatureNotImplementedException("Feature not Implemented");
  }

  @Override
  public void deleteBudget(String budgetId, String userEmail) {
    throw new FeatureNotImplementedException("Feature not Implemented");
  }

  private void validateExpenseCategory(Category category) {
    if (category.getTransactionType() != TransactionType.EXPENSE) {
      throw new IllegalArgumentException("Budgets can only be created for expense categories");
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

  private BudgetResponse mapToBudgetResponse(User user, Budget savedBudget) {
    BigDecimal spentAmount =
        transactionRepository.sumExpenseByUserAndCategoryAndMonthAndYear(
            user,
            savedBudget.getCategory(),
            savedBudget.getMonth().getValue(),
            savedBudget.getYear());
    BigDecimal remainingAmount = savedBudget.getAmount().subtract(spentAmount);
    return BudgetResponse.builder()
        .id(savedBudget.getId())
        .categoryName(savedBudget.getCategory().getName())
        .amount(savedBudget.getAmount())
        .month(savedBudget.getMonth())
        .year(savedBudget.getYear())
        .remainingAmount(remainingAmount)
        .spentAmount(spentAmount)
        .updatedAt(savedBudget.getUpdatedAt())
        .createdAt(savedBudget.getCreatedAt())
        .build();
  }

  private Budget mapToBudget(
      CreateBudgetRequest request, User authenticatedUser, Category category) {
    return Budget.builder()
        .amount(request.getAmount())
        .month(request.getMonth())
        .year(request.getYear())
        .category(category)
        .user(authenticatedUser)
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
