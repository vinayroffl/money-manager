package com.vinay.moneymanager.transaction.repository;

import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.transaction.entity.Transaction;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import com.vinay.moneymanager.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

  Optional<Transaction> findByIdAndUser(UUID transactionId, User user);

  List<Transaction> findAllByUser(User user);

  List<Transaction> findAllByUserAndCategory(User user, Category category);

  List<Transaction> findAllByUserAndType(User user, TransactionType transactionType);
}
