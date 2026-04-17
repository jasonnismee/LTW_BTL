package com.restaurant.model.dto;

import com.restaurant.model.enums.UserRank;
import com.restaurant.model.enums.VoucherType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class VoucherDTO {
  @NotBlank
  @Size(min = 4, max = 20)
  @Pattern(regexp = "^[A-Za-z0-9_]{4,20}$")
  private String code;

  @NotNull
  @Min(1)
  @Max(100)
  private Integer discountPercent;

  private BigDecimal maxDiscountAmount;

  @NotNull
  private BigDecimal minOrderAmount = BigDecimal.ZERO;

  @NotNull
  private VoucherType type;

  private UserRank minRank;

  @NotNull
  private Integer quantity;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime startDate;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime endDate;

  @NotNull
  private Boolean status = Boolean.TRUE;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Integer getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(Integer discountPercent) {
    this.discountPercent = discountPercent;
  }

  public BigDecimal getMaxDiscountAmount() {
    return maxDiscountAmount;
  }

  public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
    this.maxDiscountAmount = maxDiscountAmount;
  }

  public BigDecimal getMinOrderAmount() {
    return minOrderAmount;
  }

  public void setMinOrderAmount(BigDecimal minOrderAmount) {
    this.minOrderAmount = minOrderAmount;
  }

  public VoucherType getType() {
    return type;
  }

  public void setType(VoucherType type) {
    this.type = type;
  }

  public UserRank getMinRank() {
    return minRank;
  }

  public void setMinRank(UserRank minRank) {
    this.minRank = minRank;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public LocalDateTime getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDateTime startDate) {
    this.startDate = startDate;
  }

  public LocalDateTime getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDateTime endDate) {
    this.endDate = endDate;
  }

  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }
}

