package com.restaurant.security;

import com.restaurant.model.entity.User;
import com.restaurant.model.enums.UserStatus;
import com.restaurant.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));
    if (user.getStatus() == UserStatus.BANNED) {
      throw new DisabledException("Tài khoản đã bị khóa");
    }
    return new CustomUserDetails(user);
  }
}

