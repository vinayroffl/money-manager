package com.vinay.moneymanager.transaction.service.impl;

import com.vinay.moneymanager.common.exception.ResourceNotFoundException;
import com.vinay.moneymanager.transaction.dto.request.TransactionRequest;
import com.vinay.moneymanager.transaction.dto.response.CategoryResponse;
import com.vinay.moneymanager.transaction.dto.response.TransactionResponse;
import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.transaction.entity.Transaction;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import com.vinay.moneymanager.transaction.repository.CategoryRepository;
import com.vinay.moneymanager.transaction.repository.TransactionRepository;
import com.vinay.moneymanager.transaction.service.TransactionService;
import com.vinay.moneymanager.user.entity.User;
import com.vinay.moneymanager.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final TransactionRepository transactionRepository;

  @Override
  public TransactionResponse createTransaction(TransactionRequest request, String email) {

    User user = getAuthenticatedUser(email);
    Category category = getCategory(request.getCategoryId());
    LocalDateTime transactionDate =
        request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now();

    Transaction transaction = mapToEntity(request, user, category, transactionDate);
    Transaction savedTransaction = transactionRepository.save(transaction);

    return mapToResponse(savedTransaction);
  }

  @Override
  public TransactionResponse getTransaction(UUID transactionId, String email) {

    User user = getAuthenticatedUser(email);
    Transaction transaction =
        transactionRepository
            .findByIdAndUser(transactionId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

    return mapToResponse(transaction);
  }

  @Override
  public List<TransactionResponse> getTransactions(String email) {

    User user = getAuthenticatedUser(email);
    List<Transaction> transactions = transactionRepository.findAllByUser(user);

    return mapToResponseList(transactions);
  }

  @Override
  public TransactionResponse updateTransaction(
      UUID transactionId, TransactionRequest request, String email) {

    User user = getAuthenticatedUser(email);
    Transaction transaction =
        transactionRepository
            .findByIdAndUser(transactionId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

    Category category = getCategory(request.getCategoryId());
    LocalDateTime transactionDate =
        request.getTransactionDate() != null
            ? request.getTransactionDate()
            : transaction.getTransactionDate();

    transaction.setAmount(request.getAmount());
    transaction.setType(request.getType());
    transaction.setDescription(request.getDescription());
    transaction.setTransactionDate(transactionDate);
    transaction.setCategory(category);
    Transaction updatedTransaction = transactionRepository.save(transaction);

    return mapToResponse(updatedTransaction);
  }

  @Override
  public void deleteTransaction(UUID transactionId, String email) {

    User user = getAuthenticatedUser(email);
    Transaction transaction =
        transactionRepository
            .findByIdAndUser(transactionId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

    transactionRepository.delete(transaction);
  }

  @Override
  public List<TransactionResponse> getTransactionsByCategory(String email, Integer categoryId) {

    User user = getAuthenticatedUser(email);
    Category category = getCategory(categoryId);
    List<Transaction> transactions = transactionRepository.findAllByUserAndCategory(user, category);

    return mapToResponseList(transactions);
  }

  @Override
  public List<TransactionResponse> getTransactionsByType(String email, TransactionType type) {

    User user = getAuthenticatedUser(email);
    List<Transaction> transactions = transactionRepository.findAllByUserAndType(user, type);

    return mapToResponseList(transactions);
  }

  @Override
  public List<CategoryResponse> getCategories() {
    List<Category> categories = categoryRepository.findAll();
    return categories.stream().map(this::mapToCategoryResponse).toList();
  }

  @NonNull
  private User getAuthenticatedUser(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
  }

  @NonNull
  private Category getCategory(Integer categoryId) {
    return categoryRepository
        .findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
  }

  private Transaction mapToEntity(
      TransactionRequest request, User user, Category category, LocalDateTime transactionDate) {
    return Transaction.builder()
        .amount(request.getAmount())
        .type(request.getType())
        .description(request.getDescription())
        .transactionDate(transactionDate)
        .user(user)
        .category(category)
        .build();
  }

  private TransactionResponse mapToResponse(Transaction transaction) {
    return TransactionResponse.builder()
        .id(transaction.getId())
        .amount(transaction.getAmount())
        .type(transaction.getType())
        .categoryId(transaction.getCategory().getId())
        .categoryName(transaction.getCategory().getName())
        .description(transaction.getDescription())
        .transactionDate(transaction.getTransactionDate())
        .createdAt(transaction.getCreatedAt())
        .updatedAt(transaction.getUpdatedAt())
        .build();
  }

  private List<TransactionResponse> mapToResponseList(List<Transaction> transactions) {
    return transactions.stream().map(this::mapToResponse).toList();
  }

  private CategoryResponse mapToCategoryResponse(Category category) {
    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .description(category.getDescription())
        .transactionType(category.getTransactionType())
        .build();
  }
}
