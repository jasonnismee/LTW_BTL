package com.restaurant.repository;

import com.restaurant.model.entity.User;
import com.restaurant.model.enums.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  List<User> findByRole(UserRole role);

  boolean existsByUsername(String username);

  boolean existsByPhone(String phone);

  boolean existsByEmail(String email);

  boolean existsByPhoneAndIdNot(String phone, Long id);

  boolean existsByEmailAndIdNot(String email, Long id);
}

