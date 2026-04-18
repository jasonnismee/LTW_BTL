package com.restaurant.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileUpdateDTO {
  @NotBlank
  @Size(min = 2, max = 100)
  private String fullName;

  @NotBlank
  @Pattern(regexp = "0[0-9]{9,10}")
  private String phone;

  /** Tuỳ chọn; để trống hoặc null. */
  private String email;

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
