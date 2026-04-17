package com.restaurant.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserDTO {
  @NotBlank
  @Size(min = 3, max = 30)
  private String username;

  @NotBlank
  @Size(min = 6, max = 100)
  private String password;

  @NotBlank
  @Size(min = 2, max = 100)
  private String fullName;

  @NotBlank
  @Pattern(regexp = "0[0-9]{9,10}")
  private String phone;

  @Email
  private String email;

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
}

