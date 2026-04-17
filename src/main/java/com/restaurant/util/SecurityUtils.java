package com.restaurant.util;

import com.restaurant.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
  private SecurityUtils() {}

  public static Long currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
      return null;
    }
    return cud.getUser().getId();
  }

  public static CustomUserDetails currentUserDetails() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
      return null;
    }
    return cud;
  }
}

