package com.restaurant.service;

import com.restaurant.model.dto.MenuItemDTO;
import com.restaurant.model.entity.MenuItem;
import java.util.List;

public interface MenuItemService {
  List<MenuItem> getAllAvailable();

  List<MenuItem> getAll();

  MenuItem getById(Long id);

  List<MenuItem> getByCategory(Long categoryId);

  List<MenuItem> searchByName(String keyword);

  MenuItem create(MenuItemDTO dto);

  MenuItem update(Long id, MenuItemDTO dto);

  MenuItem toggleStatus(Long id);

  void delete(Long id);
}

