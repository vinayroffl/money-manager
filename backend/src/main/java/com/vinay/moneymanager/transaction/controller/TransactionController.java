package com.vinay.moneymanager.transaction.controller;

import com.vinay.moneymanager.common.response.ApiResponse;
import com.vinay.moneymanager.transaction.dto.request.TransactionRequest;
import com.vinay.moneymanager.transaction.dto.response.TransactionResponse;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import com.vinay.moneymanager.transaction.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;

  @PostMapping
  public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
      @Valid @RequestBody TransactionRequest request, Authentication authentication) {

    String email = getUsernameFromAuthentication(authentication);
    var transactionResponse = transactionService.createTransaction(request, email);
    var apiResponse =
        buildSuccessResponse(transactionResponse, "Transaction was added successfully");
    return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
      @PathVariable UUID id, Authentication authentication) {

    String email = getUsernameFromAuthentication(authentication);
    var transactionResponse = transactionService.getTransaction(id, email);
    var apiResponse = buildSuccessResponse(transactionResponse, "Transaction fetched successfully");
    return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions(
      Authentication authentication) {

    String email = getUsernameFromAuthentication(authentication);
    var usersTransactions = transactionService.getTransactions(email);
    var apiResponse = buildSuccessResponse(usersTransactions, "Transactions fetched successfully");
    return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
      @Valid @RequestBody TransactionRequest request,
      @PathVariable UUID id,
      Authentication authentication) {

    String email = getUsernameFromAuthentication(authentication);
    var updatedTransaction = transactionService.updateTransaction(id, request, email);
    var apiResponse =
        buildSuccessResponse(updatedTransaction, "Transaction was updated successfully");
    return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Object>> deleteTransaction(
      @PathVariable UUID id, Authentication authentication) {

    String email = getUsernameFromAuthentication(authentication);
    transactionService.deleteTransaction(id, email);
    var apiResponse = buildSuccessResponse(null, "Transaction deleted successfully");
    return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
  }

  @GetMapping("/category/{categoryId}")
  public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByCategory(
      @PathVariable Integer categoryId, Authentication authentication) {

    String email = getUsernameFromAuthentication(authentication);
    var transactionsByCategory = transactionService.getTransactionsByCategory(email, categoryId);
    var apiResponse =
        buildSuccessResponse(transactionsByCategory, "Transactions fetched successfully");
    return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
  }

  @GetMapping("/type/{type}")
  public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByType(
      @PathVariable TransactionType type, Authentication authentication) {

    String email = getUsernameFromAuthentication(authentication);
    var transactionsByType = transactionService.getTransactionsByType(email, type);
    var apiResponse = buildSuccessResponse(transactionsByType, "Transactions fetched successfully");
    return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
  }

  private <T> ApiResponse<T> buildSuccessResponse(T data, String message) {
    return ApiResponse.<T>builder().success(true).message(message).data(data).build();
  }

  private String getUsernameFromAuthentication(Authentication authentication) {
    return authentication.getName();
  }
}
