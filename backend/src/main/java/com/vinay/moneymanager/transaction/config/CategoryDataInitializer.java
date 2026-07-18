package com.vinay.moneymanager.transaction.config;

import com.vinay.moneymanager.transaction.entity.Category;
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
  public void run(String... args) throws Exception {

    List<Category> defaultCategories =
        List.of(
            Category.builder().name("Salary").description("Salary category").build(),
            Category.builder().name("Food").description("Food category").build(),
            Category.builder().name("Grocery").description("Grocery category").build(),
            Category.builder().name("Fuel").description("Fuel category").build(),
            Category.builder().name("Shopping").description("Shopping category").build(),
            Category.builder().name("Bills").description("Bills category").build(),
            Category.builder().name("Entertainment").description("Entertainment category").build(),
            Category.builder().name("Health").description("Health category").build(),
            Category.builder().name("Travel").description("Travel category").build(),
            Category.builder().name("Other").description("Other category").build());

    defaultCategories.forEach(
        defaultCategory -> {
          if (!categoryRepository.existsByName(defaultCategory.getName())) {
            categoryRepository.save(defaultCategory);
          }
        });
  }
}
