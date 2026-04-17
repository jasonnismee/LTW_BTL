package com.restaurant.model.entity;

import com.restaurant.model.enums.UserRank;
import com.restaurant.model.enums.VoucherType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "vouchers",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_vouchers_code", columnNames = "code")
    }
)
public class Voucher extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(min = 4, max = 20)
  @Pattern(regexp = "^[A-Z0-9_]{4,20}$")
  @Column(nullable = false, length = 20)
  private String code;

  @NotNull
  @Min(1)
  @Max(100)
  @Column(nullable = false)
  private Integer discountPercent;

  @Column(nullable = true, precision = 19, scale = 2)
  private BigDecimal maxDiscountAmount;

  @NotNull
  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal minOrderAmount = BigDecimal.ZERO;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private VoucherType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = true, length = 20)
  private UserRank minRank;

  @NotNull
  @Column(nullable = false)
  private Integer quantity;

  @NotNull
  @Column(nullable = false)
  private Integer usedCount = 0;

  @NotNull
  @Column(nullable = false)
  private LocalDateTime startDate;

  @NotNull
  @Column(nullable = false)
  private LocalDateTime endDate;

  @NotNull
  @Column(nullable = false)
  private Boolean status = Boolean.TRUE;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public Integer getUsedCount() {
    return usedCount;
  }

  public void setUsedCount(Integer usedCount) {
    this.usedCount = usedCount;
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

