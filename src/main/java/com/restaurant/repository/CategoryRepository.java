package com.restaurant.repository;

import com.restaurant.model.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  Optional<Category> findByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCase(String name);
}

