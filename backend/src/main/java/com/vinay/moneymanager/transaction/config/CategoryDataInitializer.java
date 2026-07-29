package com.vinay.moneymanager.transaction.config;

import com.vinay.moneymanager.transaction.entity.Category;
import com.vinay.moneymanager.transaction.entity.TransactionType;
import com.vinay.moneymanager.transaction.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements CommandLineRunner {

  private final CategoryRepository categoryRepository;

  @Override
  public void run(String... args) {

    List<Category> defaultCategories =
        List.of(
            Category.builder()
                .name("Salary")
                .description("Salary category")
                .transactionType(TransactionType.INCOME)
                .build(),
            Category.builder()
                .name("Food")
                .description("Food category")
                .transactionType(TransactionType.EXPENSE)
                .build(),
            Category.builder()
                .name("Grocery")
                .description("Grocery category")
                .transactionType(TransactionType.EXPENSE)
                .build(),
            Category.builder()
                .name("Fuel")
                .description("Fuel category")
                .transactionType(TransactionType.EXPENSE)
                .build(),
            Category.builder()
                .name("Shopping")
                .description("Shopping category")
                .transactionType(TransactionType.EXPENSE)
                .build(),
            Category.builder()
                .name("Bills")
                .description("Bills category")
                .transactionType(TransactionType.EXPENSE)
                .build(),
            Category.builder()
                .name("Entertainment")
                .description("Entertainment category")
                .transactionType(TransactionType.EXPENSE)
                .build(),
            Category.builder()
                .name("Health")
                .description("Health category")
                .transactionType(TransactionType.EXPENSE)
                .build(),
            Category.builder()
                .name("Travel")
                .description("Travel category")
                .transactionType(TransactionType.EXPENSE)
                .build(),
            Category.builder()
                .name("Other")
                .description("Other category")
                .transactionType(TransactionType.EXPENSE)
                .build());

    defaultCategories.forEach(
        defaultCategory -> {
          if (!categoryRepository.existsByName(defaultCategory.getName())) {
            categoryRepository.save(defaultCategory);
          }
        });
  }
}
