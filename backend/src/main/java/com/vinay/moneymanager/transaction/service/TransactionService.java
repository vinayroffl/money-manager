package com.vinay.moneymanager.transaction.service;

import com.vinay.moneymanager.transaction.dto.request.TransactionRequest;
import com.vinay.moneymanager.transaction.dto.response.TransactionResponse;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

  TransactionResponse createTransaction(TransactionRequest request, String email);

  TransactionResponse getTransaction(UUID transactionId, String email);

  List<TransactionResponse> getTransactions(String email);

  TransactionResponse updateTransaction(
      UUID transactionId, TransactionRequest request, String email);

  void deleteTransaction(UUID transactionId, String email);

  List<TransactionResponse> getTransactionsByCategory(String email, Integer categoryId);

  List<TransactionResponse> getTransactionsByType(String email, TransactionType type);
}
