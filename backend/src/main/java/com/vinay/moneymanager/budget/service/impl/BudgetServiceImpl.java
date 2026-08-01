package com.vinay.moneymanager.budget.service.impl;

import com.vinay.moneymanager.budget.dto.request.CreateBudgetRequest;
import com.vinay.moneymanager.budget.dto.request.UpdateBudgetRequest;
import com.vinay.moneymanager.budget.dto.response.BudgetResponse;
import com.vinay.moneymanager.budget.entity.Budget;
import com.vinay.moneymanager.budget.repository.BudgetRepository;
import com.vinay.moneymanager.budget.service.BudgetService;
import com.vinay.moneymanager.common.exception.DuplicateResourceException;
import com.vinay.moneymanager.common.exception.FeatureNotImplementedException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
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
    BigDecimal spentAmount = fetchSpentAmount(user, savedBudget);
    BigDecimal remainingAmount = savedBudget.getAmount().subtract(spentAmount);

    return mapToBudgetResponse(savedBudget, spentAmount, remainingAmount);
  }

  @Override
  public BudgetResponse getBudgetById(UUID budgetId, String userEmail) {
    User user = getAuthenticatedUser(userEmail);
    Budget budget =
        budgetRepository
            .findByIdAndUser(budgetId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

    BigDecimal spentAmount = fetchSpentAmount(user, budget);
    BigDecimal remainingAmount = budget.getAmount().subtract(spentAmount);
    return mapToBudgetResponse(budget, spentAmount, remainingAmount);
  }

  @Override
  public List<BudgetResponse> getAllBudgets(String userEmail) {
    User user = getAuthenticatedUser(userEmail);

    List<Budget> budgets = budgetRepository.findByUser(user);
    if (budgets.isEmpty()) throw new ResourceNotFoundException("No budgets found");

    return mapToListOfBudgetResponse(budgets, user);
  }

  @Override
  public List<BudgetResponse> getBudgetsByMonth(Month month, Integer year, String userEmail) {
    throw new FeatureNotImplementedException("Feature not Implemented");
  }

  @Override
  public BudgetResponse updateBudget(UUID budgetId, UpdateBudgetRequest request, String userEmail) {
    throw new FeatureNotImplementedException("Feature not Implemented");
  }

  @Override
  public void deleteBudget(UUID budgetId, String userEmail) {
    throw new FeatureNotImplementedException("Feature not Implemented");
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

  private BudgetResponse mapToBudgetResponse(
      Budget savedBudget, BigDecimal spentAmount, BigDecimal remainingAmount) {

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

  private List<BudgetResponse> mapToListOfBudgetResponse(List<Budget> budgets, User user) {
    List<BudgetResponse> budgetResponseList = new ArrayList<>();
    for (Budget budget : budgets) {
      BigDecimal spentAmount = fetchSpentAmount(user, budget);
      BigDecimal remainingAmount = budget.getAmount().subtract(spentAmount);
      budgetResponseList.add(mapToBudgetResponse(budget, spentAmount, remainingAmount));
    }
    return budgetResponseList;
  }

  private BigDecimal fetchSpentAmount(User user, Budget savedBudget) {
    return transactionRepository.sumExpenseByUserAndCategoryAndMonthAndYear(
        user, savedBudget.getCategory(), savedBudget.getMonth().getValue(), savedBudget.getYear());
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
