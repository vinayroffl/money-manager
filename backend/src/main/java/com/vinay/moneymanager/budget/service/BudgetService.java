package com.vinay.moneymanager.budget.service;

import com.vinay.moneymanager.budget.dto.request.CreateBudgetRequest;
import com.vinay.moneymanager.budget.dto.request.UpdateBudgetRequest;
import com.vinay.moneymanager.budget.dto.response.BudgetResponse;
import java.time.Month;
import java.util.List;
import java.util.UUID;

public interface BudgetService {

  BudgetResponse createBudget(CreateBudgetRequest createBudgetRequest, String userEmail);

  BudgetResponse getBudgetById(UUID budgetId, String userEmail);

  List<BudgetResponse> getAllBudgets(String userEmail);

  List<BudgetResponse> getBudgetsByMonth(Month month, Integer year, String userEmail);

  BudgetResponse updateBudget(UUID budgetId, UpdateBudgetRequest request, String userEmail);

  void deleteBudget(String budgetId, String userEmail);
}
