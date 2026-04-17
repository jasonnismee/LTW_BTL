package com.restaurant.model.dto;

import java.math.BigDecimal;

public class VoucherResult {
  private boolean valid;
  private BigDecimal discountAmount;
  private BigDecimal finalPrice;
  private String message;

  public VoucherResult() {}

  public VoucherResult(boolean valid, BigDecimal discountAmount, BigDecimal finalPrice, String message) {
    this.valid = valid;
    this.discountAmount = discountAmount;
    this.finalPrice = finalPrice;
    this.message = message;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public BigDecimal getDiscountAmount() {
    return discountAmount;
  }

  public void setDiscountAmount(BigDecimal discountAmount) {
    this.discountAmount = discountAmount;
  }

  public BigDecimal getFinalPrice() {
    return finalPrice;
  }

  public void setFinalPrice(BigDecimal finalPrice) {
    this.finalPrice = finalPrice;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

