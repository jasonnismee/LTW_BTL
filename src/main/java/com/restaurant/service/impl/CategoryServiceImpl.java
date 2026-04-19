package com.restaurant.service.impl;

import com.restaurant.exception.BusinessException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.entity.Category;
import com.restaurant.repository.CategoryRepository;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.service.CategoryService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepository categoryRepository;
  private final MenuItemRepository menuItemRepository;

  public CategoryServiceImpl(CategoryRepository categoryRepository, MenuItemRepository menuItemRepository) {
    this.categoryRepository = categoryRepository;
    this.menuItemRepository = menuItemRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Category> getAllCategories() {
    return categoryRepository.findByIsDeletedFalse();
  }

  @Override
  public Category createCategory(String name) {
    String normalized = normalizeName(name);
    if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(normalized)) {
      throw new BusinessException("Tên danh mục đã tồn tại");
    }
    Category c = new Category();
    c.setName(normalized);
    return categoryRepository.save(c);
  }

  @Override
  public Category updateCategory(Long id, String name) {
    Category c = categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy category id=" + id));
    if (Boolean.TRUE.equals(c.getIsDeleted())) {
      throw new ResourceNotFoundException("Không tìm thấy category id=" + id);
    }
    String normalized = normalizeName(name);
    categoryRepository.findByNameIgnoreCaseAndIsDeletedFalse(normalized)
        .filter(existing -> !existing.getId().equals(id))
        .ifPresent(existing -> {
          throw new BusinessException("Tên danh mục đã tồn tại");
        });
    c.setName(normalized);
    return categoryRepository.save(c);
  }

  @Override
  public void deleteCategory(Long id) {
    Category c = categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy category id=" + id));
    if (Boolean.TRUE.equals(c.getIsDeleted())) {
      throw new ResourceNotFoundException("Không tìm thấy category id=" + id);
    }
    boolean hasMenuItems = menuItemRepository.findByCategoryId(id).stream().findAny().isPresent();
    if (hasMenuItems) {
      throw new BusinessException("Không thể xóa danh mục vì còn món ăn");
    }
    c.setIsDeleted(true);
    c.setName(c.getName() + "_DEL_" + System.currentTimeMillis());
    categoryRepository.save(c);
  }

  private String normalizeName(String name) {
    if (name == null || name.isBlank()) {
      throw new BusinessException("Tên danh mục không hợp lệ");
    }
    return name.trim();
  }
}
