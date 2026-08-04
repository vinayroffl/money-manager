package com.vinay.moneymanager.budget.controller;

import com.vinay.moneymanager.budget.dto.request.CreateBudgetRequest;
import com.vinay.moneymanager.budget.dto.request.UpdateBudgetRequest;
import com.vinay.moneymanager.budget.dto.response.BudgetResponse;
import com.vinay.moneymanager.budget.service.BudgetService;
import com.vinay.moneymanager.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

  private final BudgetService budgetService;

  @PostMapping
  public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
      @Valid @RequestBody CreateBudgetRequest createBudgetRequest, Authentication authentication) {
    String email = getUsernameFromAuthentication(authentication);
    var budgetResponse = budgetService.createBudget(createBudgetRequest, email);
    var apiResponse = buildSuccessResponse(budgetResponse, "Budget added successfully");
    return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
  }

  @GetMapping("/{budgetId}")
  public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(
      @PathVariable UUID budgetId, Authentication authentication) {
    String email = getUsernameFromAuthentication(authentication);
    var budgetResponse = budgetService.getBudgetById(budgetId, email);
    var apiResponse = buildSuccessResponse(budgetResponse, "Budget retrieved successfully");
    return ResponseEntity.ok().body(apiResponse);
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAllBudgets(
      Authentication authentication) {
    String email = getUsernameFromAuthentication(authentication);
    var budgetResponse = budgetService.getAllBudgets(email);
    var apiResponse = buildSuccessResponse(budgetResponse, "Budgets retrieved successfully");
    return ResponseEntity.ok().body(apiResponse);
  }

  @GetMapping("/monthly")
  public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgetsByMonth(
      @RequestParam Month month, @RequestParam Integer year, Authentication authentication) {
    String email = getUsernameFromAuthentication(authentication);
    var budgetResponse = budgetService.getBudgetsByMonth(month, year, email);
    var apiResponse =
        buildSuccessResponse(budgetResponse, "Monthly budgets retrieved successfully");
    return ResponseEntity.ok().body(apiResponse);
  }

  @PutMapping("/{budgetId}")
  public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
      @PathVariable UUID budgetId,
      @Valid @RequestBody UpdateBudgetRequest request,
      Authentication authentication) {
    String email = getUsernameFromAuthentication(authentication);
    var updatedBudget = budgetService.updateBudget(budgetId, request, email);
    var apiResponse = buildSuccessResponse(updatedBudget, "Budget updated successfully");
    return ResponseEntity.ok().body(apiResponse);
  }

  @DeleteMapping("/{budgetId}")
  public ResponseEntity<ApiResponse<Object>> deleteBudget(
      @PathVariable UUID budgetId, Authentication authentication) {
    String email = getUsernameFromAuthentication(authentication);
    budgetService.deleteBudget(budgetId, email);
    var apiResponse = buildSuccessResponse(null, "Budget deleted successfully");
    return ResponseEntity.ok().body(apiResponse);
  }

  private <T> ApiResponse<T> buildSuccessResponse(T data, String message) {
    return ApiResponse.<T>builder().success(true).message(message).data(data).build();
  }

  private String getUsernameFromAuthentication(Authentication authentication) {
    return authentication.getName();
  }
}
