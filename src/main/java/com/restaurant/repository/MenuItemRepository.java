package com.restaurant.repository;

import com.restaurant.model.entity.MenuItem;
import com.restaurant.model.enums.MenuItemStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
  List<MenuItem> findByStatus(MenuItemStatus status);

  List<MenuItem> findByStatusNot(MenuItemStatus status);

  List<MenuItem> findByCategoryId(Long categoryId);

  List<MenuItem> findByNameContainingIgnoreCase(String keyword);

  List<MenuItem> findByStatusAndNameContainingIgnoreCase(MenuItemStatus status, String keyword);

  List<MenuItem> findByStatusAndCategoryId(MenuItemStatus status, Long categoryId);
}
