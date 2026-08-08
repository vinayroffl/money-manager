package com.vinay.moneymanager.transaction.repository;

import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.transaction.entity.Transaction;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import com.vinay.moneymanager.user.entity.User;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

  Optional<Transaction> findByIdAndUser(UUID transactionId, User user);

  List<Transaction> findAllByUser(User user);

  List<Transaction> findAllByUserAndCategory(User user, Category category);

  List<Transaction> findAllByUserAndType(User user, TransactionType transactionType);

  @Query(
      """
        SELECT COALESCE(SUM(t.amount),0)
        FROM Transaction t
        WHERE t.user = :user
        AND t.category = :category
        AND MONTH(t.transactionDate) = :month
        AND YEAR(t.transactionDate) = :year
        """)
  BigDecimal sumExpenseByUserAndCategoryAndMonthAndYear(
      User user, @NotNull Category category, @NotNull Integer month, @NotNull Integer year);
}
