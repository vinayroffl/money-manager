package com.vinay.moneymanager.transaction.repository;

import com.vinay.moneymanager.transaction.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

  boolean existsByName(String name);

  Category findByName(String travel);
}
