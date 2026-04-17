package com.restaurant.model.entity;

import com.restaurant.model.enums.UserRank;
import com.restaurant.model.enums.UserRole;
import com.restaurant.model.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
      @UniqueConstraint(name = "uk_users_phone", columnNames = "phone"),
      @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    }
)
public class User extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(min = 3, max = 30)
  @Column(nullable = false, length = 30)
  private String username;

  @NotBlank
  @Column(nullable = false)
  private String password;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role;

  @NotBlank
  @Size(min = 2, max = 100)
  @Column(nullable = false, length = 100)
  private String fullName;

  @NotBlank
  @Pattern(regexp = "0[0-9]{9,10}")
  @Column(nullable = false, length = 11)
  private String phone;

  @Email
  @Column(nullable = true, length = 150)
  private String email;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true)
  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalSpending = BigDecimal.ZERO;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "user_rank", nullable = false, length = 20)
  private UserRank rank = UserRank.BRONZE;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserStatus status = UserStatus.ACTIVE;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public BigDecimal getTotalSpending() {
    return totalSpending;
  }

  public void setTotalSpending(BigDecimal totalSpending) {
    this.totalSpending = totalSpending;
  }

  public UserRank getRank() {
    return rank;
  }

  public void setRank(UserRank rank) {
    this.rank = rank;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }
}

