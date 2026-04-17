package com.restaurant.service;

import com.restaurant.model.dto.UserDTO;
import com.restaurant.model.entity.User;
import com.restaurant.model.enums.UserRole;
import java.math.BigDecimal;
import java.util.List;

public interface UserService {
  User createUser(UserDTO userDTO);

  User findByUsername(String username);

  User findById(Long id);

  List<User> findByRole(UserRole role);

  void banUser(Long id);

  void unbanUser(Long id);

  User updateTotalSpendingAndRank(Long userId, BigDecimal amount);
}

