package com.restaurant.service;

import com.restaurant.model.entity.Category;
import java.util.List;

public interface CategoryService {
  List<Category> getAllCategories();

  Category createCategory(String name);

  Category updateCategory(Long id, String name);

  void deleteCategory(Long id);
}

