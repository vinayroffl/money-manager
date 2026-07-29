package com.vinay.moneymanager.budget.repository;

import com.vinay.moneymanager.budget.entity.Budget;
import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.user.entity.User;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

  Optional<Budget> findByUserAndCategoryAndMonthAndYear(
      User user, Category category, Month month, int year);

  List<Budget> findByUser(User user);

  List<Budget> findByUserAndMonthAndYear(User user, Month month, int year);
}
