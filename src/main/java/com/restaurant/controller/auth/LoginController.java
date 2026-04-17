package com.restaurant.controller.auth;

import com.restaurant.model.enums.UserRole;
import com.restaurant.util.AppConstants;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class LoginController {
  @GetMapping("/login")
  public String login() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.ADMIN));
      boolean isStaff = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.STAFF));
      boolean isCustomer = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.CUSTOMER));

      if (isAdmin) return "redirect:" + AppConstants.Routes.ADMIN_DASHBOARD;
      if (isStaff) return "redirect:" + AppConstants.Routes.STAFF_POS;
      if (isCustomer) return "redirect:" + AppConstants.Routes.CUSTOMER_HOME;
    }
    return "auth/login";
  }
}

