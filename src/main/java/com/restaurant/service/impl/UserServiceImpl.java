package com.restaurant.service.impl;

import com.restaurant.exception.BusinessException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.dto.UserDTO;
import com.restaurant.model.entity.User;
import com.restaurant.model.enums.UserRank;
import com.restaurant.model.enums.UserRole;
import com.restaurant.model.enums.UserStatus;
import com.restaurant.repository.UserRepository;
import com.restaurant.service.UserService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
  private static final BigDecimal THRESHOLD_SILVER = new BigDecimal("5000000");
  private static final BigDecimal THRESHOLD_GOLD = new BigDecimal("15000000");
  private static final BigDecimal THRESHOLD_DIAMOND = new BigDecimal("30000000");

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public User createUser(UserDTO userDTO) {
    if (userRepository.existsByUsername(userDTO.getUsername())) {
      throw new BusinessException("Username đã tồn tại");
    }
    if (userRepository.existsByPhone(userDTO.getPhone())) {
      throw new BusinessException("Số điện thoại đã tồn tại");
    }
    if (userDTO.getEmail() != null && !userDTO.getEmail().isBlank() && userRepository.existsByEmail(userDTO.getEmail())) {
      throw new BusinessException("Email đã tồn tại");
    }

    User user = new User();
    user.setUsername(userDTO.getUsername());
    user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
    user.setRole(UserRole.CUSTOMER);
    user.setFullName(userDTO.getFullName());
    user.setPhone(userDTO.getPhone());
    user.setEmail((userDTO.getEmail() == null || userDTO.getEmail().isBlank()) ? null : userDTO.getEmail());
    user.setStatus(UserStatus.ACTIVE);
    user.setRank(UserRank.BRONZE);
    user.setTotalSpending(BigDecimal.ZERO);
    return userRepository.save(user);
  }

  @Override
  @Transactional(readOnly = true)
  public User findByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));
  }

  @Override
  @Transactional(readOnly = true)
  public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user id=" + id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findByRole(UserRole role) {
    return userRepository.findByRole(role);
  }

  @Override
  public void banUser(Long id) {
    User user = findById(id);
    user.setStatus(UserStatus.BANNED);
    userRepository.save(user);
  }

  @Override
  public void unbanUser(Long id) {
    User user = findById(id);
    user.setStatus(UserStatus.ACTIVE);
    userRepository.save(user);
  }

  @Override
  public User updateTotalSpendingAndRank(Long userId, BigDecimal amount) {
    // Loyalty loop (BRIDGE 3):
    // - Khi đơn ONLINE hoàn tất (COMPLETED), cộng dồn finalPrice vào totalSpending.
    // - Tự động thăng hạng theo ngưỡng cộng dồn (chỉ thăng, tuyệt đối không hạ hạng).
    if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessException("Số tiền cộng dồn không hợp lệ");
    }

    User user = findById(userId);
    BigDecimal current = user.getTotalSpending() == null ? BigDecimal.ZERO : user.getTotalSpending();
    BigDecimal updated = current.add(amount);
    user.setTotalSpending(updated);

    UserRank newRank = calculateRank(updated);
    if (newRank.ordinal() > user.getRank().ordinal()) {
      user.setRank(newRank);
    }

    return userRepository.save(user);
  }

  private UserRank calculateRank(BigDecimal totalSpending) {
    // Ngưỡng tính theo tổng chi tiêu CỘNG DỒN, mốc đạt là ">" (strictly greater) theo đề bài.
    if (totalSpending.compareTo(THRESHOLD_DIAMOND) > 0) {
      return UserRank.DIAMOND;
    }
    if (totalSpending.compareTo(THRESHOLD_GOLD) > 0) {
      return UserRank.GOLD;
    }
    if (totalSpending.compareTo(THRESHOLD_SILVER) > 0) {
      return UserRank.SILVER;
    }
    return UserRank.BRONZE;
  }
}

