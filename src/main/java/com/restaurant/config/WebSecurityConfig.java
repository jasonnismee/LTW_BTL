package com.restaurant.config;

import com.restaurant.model.enums.UserRole;
import com.restaurant.util.AppConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public AuthenticationSuccessHandler roleBasedSuccessHandler() {
    return new AuthenticationSuccessHandler() {
      @Override
      public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
          throws IOException, ServletException {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.ADMIN));
        boolean isStaff = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.STAFF));
        boolean isCustomer = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.CUSTOMER));

        if (isAdmin) {
          response.sendRedirect(AppConstants.Routes.ADMIN_DASHBOARD);
          return;
        }
        if (isStaff) {
          response.sendRedirect(AppConstants.Routes.STAFF_POS);
          return;
        }
        if (isCustomer) {
          response.sendRedirect(AppConstants.Routes.CUSTOMER_HOME);
          return;
        }
        response.sendRedirect("/");
      }
    };
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationSuccessHandler roleBasedSuccessHandler) throws Exception {
    http
        .csrf(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/auth/**", "/uploads/**", "/css/**", "/js/**").permitAll()
            .requestMatchers("/admin/**").hasRole(UserRole.ADMIN.name())
            .requestMatchers("/staff/**").hasRole(UserRole.STAFF.name())
            .requestMatchers("/customer/**").hasRole(UserRole.CUSTOMER.name())
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage(AppConstants.Routes.AUTH_LOGIN)
            .loginProcessingUrl(AppConstants.Routes.AUTH_LOGIN)
            .successHandler(roleBasedSuccessHandler)
            .failureUrl(AppConstants.Routes.AUTH_LOGIN + "?error=true")
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl(AppConstants.Routes.AUTH_LOGOUT)
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")
            .logoutSuccessUrl(AppConstants.Routes.AUTH_LOGIN)
        )
        .sessionManagement(session -> session
            .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
        );

    return http.build();
  }
}

