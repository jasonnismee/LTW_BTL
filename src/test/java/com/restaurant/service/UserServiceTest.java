package com.restaurant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.restaurant.model.entity.User;
import com.restaurant.model.enums.UserRank;
import com.restaurant.repository.UserRepository;
import com.restaurant.service.impl.UserServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    userService = new UserServiceImpl(userRepository, passwordEncoder);
  }

  @Test
  void testUpdateTotalSpendingAndRank_BronzeToSilver() {
    User u = baseUser(UserRank.BRONZE, new BigDecimal("0"));
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User updated = userService.updateTotalSpendingAndRank(1L, new BigDecimal("6000000"));
    assertEquals(new BigDecimal("6000000"), updated.getTotalSpending());
    assertEquals(UserRank.SILVER, updated.getRank());
  }

  @Test
  void testUpdateTotalSpendingAndRank_SilverToGold() {
    User u = baseUser(UserRank.SILVER, new BigDecimal("6000000"));
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User updated = userService.updateTotalSpendingAndRank(1L, new BigDecimal("10000000"));
    assertEquals(new BigDecimal("16000000"), updated.getTotalSpending());
    assertEquals(UserRank.GOLD, updated.getRank());
  }

  @Test
  void testUpdateTotalSpendingAndRank_GoldToDiamond() {
    User u = baseUser(UserRank.GOLD, new BigDecimal("16000000"));
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User updated = userService.updateTotalSpendingAndRank(1L, new BigDecimal("20000000"));
    assertEquals(new BigDecimal("36000000"), updated.getTotalSpending());
    assertEquals(UserRank.DIAMOND, updated.getRank());
  }

  @Test
  void testUpdateTotalSpendingAndRank_NoRankDowngrade() {
    User u = baseUser(UserRank.DIAMOND, new BigDecimal("31000000"));
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User updated = userService.updateTotalSpendingAndRank(1L, new BigDecimal("1000"));
    assertEquals(UserRank.DIAMOND, updated.getRank());
  }

  private User baseUser(UserRank rank, BigDecimal total) {
    User u = new User();
    u.setId(1L);
    u.setRank(rank);
    u.setTotalSpending(total);
    return u;
  }
}

