package com.vinay.moneymanager.transaction.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.vinay.moneymanager.common.exception.ResourceNotFoundException;
import com.vinay.moneymanager.transaction.dto.request.TransactionRequest;
import com.vinay.moneymanager.transaction.dto.response.TransactionResponse;
import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.transaction.entity.Transaction;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import com.vinay.moneymanager.transaction.repository.CategoryRepository;
import com.vinay.moneymanager.transaction.repository.TransactionRepository;
import com.vinay.moneymanager.user.entity.User;
import com.vinay.moneymanager.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

  @Mock private UserRepository userRepository;

  @Mock private CategoryRepository categoryRepository;

  @Mock private TransactionRepository transactionRepository;

  @InjectMocks private TransactionServiceImpl transactionService;

  @Captor private ArgumentCaptor<Transaction> transactionCaptor;

  @Test
  void shouldCreateTransactionSuccessfully() {
    TransactionRequest request = createTransactionRequest();
    Category category = getCategory("Transportation");
    User user = getUser();
    Transaction savedTransaction = getSavedTransaction(request, category, user);

    when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

    TransactionResponse transactionResponse =
        transactionService.createTransaction(request, user.getEmail());

    verify(transactionRepository).save(transactionCaptor.capture());
    Transaction capturedTransaction = transactionCaptor.getValue();

    assertEquals(user, capturedTransaction.getUser());
    assertEquals(category, capturedTransaction.getCategory());

    assertEquals(category.getId(), capturedTransaction.getCategory().getId());
    assertEquals(request.getAmount(), capturedTransaction.getAmount());

    assertNotNull(transactionResponse);
    assertEquals(transactionResponse.getAmount(), request.getAmount());
    assertEquals(transactionResponse.getCategoryId(), request.getCategoryId());
    assertEquals(transactionResponse.getDescription(), request.getDescription());
  }

  @Test
  void shouldThrowExceptionWhenAuthenticatedUserNotFound() {

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () ->
                transactionService.createTransaction(
                    createTransactionRequest(), getUser().getEmail()));

    assertEquals("Authenticated user not found", exception.getMessage());

    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void shouldThrowExceptionWhenCategoryNotFound() {
    TransactionRequest request = createTransactionRequest();
    User user = getUser();

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> transactionService.createTransaction(request, user.getEmail()));
    assertEquals("Category not found", exception.getMessage());
    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void shouldUseCurrentDateWhenTransactionDateIsNull() {

    LocalDateTime before = LocalDateTime.now();

    TransactionRequest request = createTransactionRequest();
    Category category = getCategory("Transportation");
    User user = getUser();

    Transaction savedTransaction = getSavedTransaction(request, category, user);
    request.setTransactionDate(null);

    LocalDateTime after = LocalDateTime.now();

    when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

    TransactionResponse transactionResponse =
        transactionService.createTransaction(request, user.getEmail());

    verify(transactionRepository).save(transactionCaptor.capture());
    Transaction capturedTransaction = transactionCaptor.getValue();

    assertNotNull(capturedTransaction.getTransactionDate());
    assertTrue(capturedTransaction.getTransactionDate().isAfter(before.minusSeconds(1)));
    assertTrue(capturedTransaction.getTransactionDate().isBefore(after.plusSeconds(1)));
  }

  @Test
  void shouldUseProvidedTransactionDate() {
    TransactionRequest request = createTransactionRequest();
    LocalDateTime customTransactionDate = LocalDateTime.of(2018, Month.APRIL, 1, 0, 0, 0);
    request.setTransactionDate(customTransactionDate);
    Category category = getCategory("Transportation");
    User user = getUser();

    Transaction savedTransaction = getSavedTransaction(request, category, user);

    when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

    TransactionResponse transactionResponse =
        transactionService.createTransaction(request, user.getEmail());

    verify(transactionRepository).save(transactionCaptor.capture());
    Transaction capturedTransaction = transactionCaptor.getValue();

    assertEquals(customTransactionDate, capturedTransaction.getTransactionDate());
    assertEquals(customTransactionDate, transactionResponse.getTransactionDate());
  }

  @Test
  void shouldReturnTransactionSuccessfully() {

    User user = getUser();
    Transaction savedTransaction =
        getSavedTransaction(createTransactionRequest(), getCategory("Transportation"), user);

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.findByIdAndUser(savedTransaction.getId(), user))
        .thenReturn(Optional.of(savedTransaction));

    TransactionResponse transactionResponse =
        transactionService.getTransaction(savedTransaction.getId(), user.getEmail());

    assertEquals(savedTransaction.getId(), transactionResponse.getId());
    assertEquals(savedTransaction.getTransactionDate(), transactionResponse.getTransactionDate());
    assertEquals(savedTransaction.getAmount(), transactionResponse.getAmount());
    assertEquals(savedTransaction.getDescription(), transactionResponse.getDescription());
  }

  @Test
  void shouldThrowExceptionWhenTransactionNotFound() {
    User user = getUser();
    UUID transactionId = UUID.randomUUID();

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.findByIdAndUser(transactionId, user)).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> transactionService.getTransaction(transactionId, user.getEmail()));

    assertEquals("Transaction not found", exception.getMessage());
    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void shouldThrowExceptionWhenAuthenticatedUserNotFoundWhileGettingTransaction() {

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () ->
                transactionService.getTransaction(
                    UUID.randomUUID(), "non-existing-email@test.com"));

    assertEquals("Authenticated user not found", exception.getMessage());
    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void shouldReturnAllTransactions() {

    TransactionRequest request = createTransactionRequest();
    Category category1 = getCategory("Transportation");
    Category category2 = getCategory("Food");
    category2.setId(2);
    Category category3 = getCategory("Transportation");
    category3.setId(3);
    User user = getUser();

    List<Transaction> savedTransactions =
        List.of(
            getSavedTransaction(request, category1, user),
            getSavedTransaction(request, category2, user),
            getSavedTransaction(request, category3, user));

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.findAllByUser(user)).thenReturn(savedTransactions);

    List<TransactionResponse> transactionResponses =
        transactionService.getTransactions(user.getEmail());
    assertEquals(savedTransactions.size(), transactionResponses.size());
    assertEquals(
        savedTransactions.getFirst().getAmount(), transactionResponses.getFirst().getAmount());
  }

  @Test
  void shouldReturnEmptyListWhenNoTransactionsExist() {
    List<Transaction> savedTransactions = Collections.emptyList();
    User user = getUser();
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.findAllByUser(user)).thenReturn(savedTransactions);

    List<TransactionResponse> transactionResponses =
        transactionService.getTransactions(user.getEmail());
    assertEquals(Collections.emptyList(), transactionResponses);
  }

  @Test
  void shouldThrowExceptionWhenAuthenticatedUserNotFoundWhileGettingTransactions() {

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> transactionService.getTransactions(getUser().getEmail()));
    assertEquals("Authenticated user not found", exception.getMessage());
  }

  @Test
  void shouldUpdateTransactionSuccessfully() {
    TransactionRequest request = createTransactionRequest();
    Category category1 = getCategory("Transportation");
    User user = getUser();
    Transaction transaction = getSavedTransaction(request, category1, user);

    Transaction updatedTransaction = getSavedTransaction(request, category1, user);
    updatedTransaction.setId(transaction.getId());
    updatedTransaction.setAmount(BigDecimal.valueOf(200));
    updatedTransaction.setDescription("Cashback");
    updatedTransaction.setCategory(getCategory("Food"));
    updatedTransaction.setType(TransactionType.INCOME);

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(categoryRepository.findById(category1.getId())).thenReturn(Optional.of(category1));
    when(transactionRepository.findByIdAndUser(transaction.getId(), user))
        .thenReturn(Optional.of(transaction));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransaction);

    TransactionResponse transactionResponse =
        transactionService.updateTransaction(transaction.getId(), request, user.getEmail());

    assertEquals(updatedTransaction.getId(), transactionResponse.getId());
    assertEquals(updatedTransaction.getAmount(), transactionResponse.getAmount());
    assertEquals(updatedTransaction.getDescription(), transactionResponse.getDescription());
    assertEquals(updatedTransaction.getType(), transactionResponse.getType());

    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingTransactionNotFound() {

    TransactionRequest transactionRequest = createTransactionRequest();
    UUID transactionId = UUID.randomUUID();
    User user = getUser();
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.findByIdAndUser(transactionId, user)).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () ->
                transactionService.updateTransaction(
                    transactionId, transactionRequest, user.getEmail()));
    assertEquals("Transaction not found", exception.getMessage());
    verify(transactionRepository).findByIdAndUser(transactionId, user);
    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingWithInvalidCategory() {
    TransactionRequest transactionRequest = createTransactionRequest();
    User user = getUser();
    Transaction transaction = getSavedTransaction(transactionRequest, getCategory("Food"), user);
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.findByIdAndUser(transaction.getId(), user))
        .thenReturn(Optional.of(transaction));
    when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () ->
                transactionService.updateTransaction(
                    transaction.getId(), transactionRequest, user.getEmail()));
    assertEquals("Category not found", exception.getMessage());
  }

  @Test
  void shouldUseOriginalDateWhenUpdatingWithoutTransactionDate() {
    TransactionRequest request = createTransactionRequest();
    Category category1 = getCategory("Transportation");
    User user = getUser();
    Transaction transaction = getSavedTransaction(request, category1, user);

    Transaction updatedTransaction = getSavedTransaction(request, category1, user);
    updatedTransaction.setId(transaction.getId());
    request.setTransactionDate(null);
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(categoryRepository.findById(category1.getId())).thenReturn(Optional.of(category1));
    when(transactionRepository.findByIdAndUser(transaction.getId(), user))
        .thenReturn(Optional.of(transaction));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransaction);

    TransactionResponse transactionResponse =
        transactionService.updateTransaction(transaction.getId(), request, user.getEmail());

    assertEquals(updatedTransaction.getId(), transactionResponse.getId());
    assertEquals(updatedTransaction.getTransactionDate(), transaction.getTransactionDate());
  }

  @Test
  void shouldDeleteTransactionSuccessfully() {
    TransactionRequest request = createTransactionRequest();
    Category category1 = getCategory("Transportation");
    User user = getUser();
    Transaction transaction = getSavedTransaction(request, category1, user);

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.findByIdAndUser(transaction.getId(), user))
        .thenReturn(Optional.of(transaction));

    transactionService.deleteTransaction(transaction.getId(), user.getEmail());

    verify(transactionRepository).delete(transaction);
  }

  @Test
  void shouldThrowExceptionWhenDeletingTransactionNotFound() {

    UUID transactionId = UUID.randomUUID();
    User user = getUser();
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(transactionRepository.findByIdAndUser(transactionId, user)).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> transactionService.deleteTransaction(transactionId, user.getEmail()));
    assertEquals("Transaction not found", exception.getMessage());
    verify(transactionRepository, never())
        .delete(
            getSavedTransaction(createTransactionRequest(), getCategory("Transportation"), user));
  }

  @Test
  void shouldThrowExceptionWhenAuthenticatedUserNotFoundWhileDeleting() {
    User user = getUser();
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> transactionService.deleteTransaction(UUID.randomUUID(), user.getEmail()));
    assertEquals("Authenticated user not found", exception.getMessage());
    verify(transactionRepository, never()).delete(any(Transaction.class));
  }

  @Test
  void shouldReturnTransactionsByCategory() {
    TransactionRequest request = createTransactionRequest();
    Category category1 = getCategory("Transportation");
    Category category2 = getCategory("Food");
    category2.setId(2);
    Category category3 = getCategory("Transportation");
    category3.setId(3);
    User user = getUser();
    List<Transaction> transportationTransactions =
        List.of(
            getSavedTransaction(request, category1, user),
            getSavedTransaction(request, category3, user));

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(categoryRepository.findById(category1.getId())).thenReturn(Optional.of(category1));
    when(transactionRepository.findAllByUserAndCategory(user, category1))
        .thenReturn(transportationTransactions);

    List<TransactionResponse> transactionsByCategory =
        transactionService.getTransactionsByCategory(user.getEmail(), category1.getId());
    assertEquals(2, transactionsByCategory.size());
    assertEquals(
        transportationTransactions.getFirst().getTransactionDate(),
        transactionsByCategory.getFirst().getTransactionDate());

    verify(transactionRepository).findAllByUserAndCategory(user, category1);
  }

  @Test
  void shouldReturnEmptyListWhenCategoryHasNoTransactions() {

    Category category1 = getCategory("Transportation");
    User user = getUser();
    List<Transaction> transportationTransactions = Collections.emptyList();

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(categoryRepository.findById(category1.getId())).thenReturn(Optional.of(category1));
    when(transactionRepository.findAllByUserAndCategory(user, category1))
        .thenReturn(transportationTransactions);

    assertEquals(
        0, transactionService.getTransactionsByCategory(user.getEmail(), category1.getId()).size());
    verify(transactionRepository).findAllByUserAndCategory(user, category1);
  }

  @Test
  void shouldThrowExceptionWhenCategoryNotFoundWhileFiltering() {

    Category category1 = getCategory("Transportation");
    User user = getUser();

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(categoryRepository.findById(category1.getId())).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> transactionService.getTransactionsByCategory(user.getEmail(), category1.getId()));
    assertEquals("Category not found", exception.getMessage());
    verify(transactionRepository, never()).findAllByUserAndCategory(user, category1);
  }

  @Test
  void shouldThrowExceptionWhenAuthenticatedUserNotFoundWhileFilteringByCategory() {

    Category category1 = getCategory("Transportation");
    User user = getUser();

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> transactionService.getTransactionsByCategory(user.getEmail(), category1.getId()));
    assertEquals("Authenticated user not found", exception.getMessage());
    verify(transactionRepository, never()).findAllByUserAndCategory(user, category1);
  }

  @Test
  void shouldReturnTransactionsByType() {

    TransactionRequest request = createTransactionRequest();
    Category category1 = getCategory("Transportation");
    Category category2 = getCategory("Food");
    category2.setId(2);
    Category category3 = getCategory("Transportation");
    category3.setId(3);
    User user = getUser();
    List<Transaction> expenseTransactions =
        List.of(
            getSavedTransaction(request, category1, user),
            getSavedTransaction(request, category2, user),
            getSavedTransaction(request, category3, user));

    TransactionType type = TransactionType.EXPENSE;

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.findAllByUserAndType(user, type)).thenReturn(expenseTransactions);

    List<TransactionResponse> transactionsByType =
        transactionService.getTransactionsByType(user.getEmail(), type);
    assertEquals(3, transactionsByType.size());
    assertEquals(
        expenseTransactions.getFirst().getTransactionDate(),
        transactionsByType.getFirst().getTransactionDate());

    verify(transactionRepository).findAllByUserAndType(user, type);
  }

  @Test
  void shouldReturnEmptyListWhenTypeHasNoTransactions() {

    User user = getUser();
    TransactionType type = TransactionType.INCOME;
    List<Transaction> transportationTransactions = Collections.emptyList();

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(transactionRepository.findAllByUserAndType(user, type))
        .thenReturn(transportationTransactions);

    assertEquals(0, transactionService.getTransactionsByType(user.getEmail(), type).size());

    verify(transactionRepository).findAllByUserAndType(user, type);
  }

  @Test
  void shouldThrowExceptionWhenAuthenticatedUserNotFoundWhileFilteringByType() {

    TransactionType type = TransactionType.INCOME;
    User user = getUser();

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> transactionService.getTransactionsByType(user.getEmail(), type));
    assertEquals("Authenticated user not found", exception.getMessage());
    verify(transactionRepository, never()).findAllByUserAndType(user, type);
  }

  private Transaction getSavedTransaction(
      TransactionRequest request, Category category, User user) {
    return Transaction.builder()
        .id(UUID.randomUUID())
        .amount(request.getAmount())
        .type(request.getType())
        .description(request.getDescription())
        .transactionDate(request.getTransactionDate())
        .category(category)
        .user(user)
        .build();
  }

  private Category getCategory(String categoryName) {
    return Category.builder().id(1).name(categoryName).description("Default description").build();
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

  private TransactionRequest createTransactionRequest() {
    return TransactionRequest.builder()
        .amount(new BigDecimal("150.62"))
        .type(TransactionType.EXPENSE)
        .categoryId(1)
        .description("Uber auto to Railway Station")
        .transactionDate(LocalDateTime.now())
        .build();
  }
}
