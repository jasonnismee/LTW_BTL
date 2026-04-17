package com.restaurant.model.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {
  private Long menuItemId;
  private String menuItemName;
  private BigDecimal unitPrice;
  private Integer quantity;
  private BigDecimal subtotal;

  public CartItem() {}

  public CartItem(Long menuItemId, String menuItemName, BigDecimal unitPrice, Integer quantity) {
    this.menuItemId = menuItemId;
    this.menuItemName = menuItemName;
    this.unitPrice = unitPrice;
    this.quantity = quantity;
    recalcSubtotal();
  }

  public void recalcSubtotal() {
    if (unitPrice == null || quantity == null) {
      this.subtotal = BigDecimal.ZERO;
      return;
    }
    this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
  }

  public Long getMenuItemId() {
    return menuItemId;
  }

  public void setMenuItemId(Long menuItemId) {
    this.menuItemId = menuItemId;
  }

  public String getMenuItemName() {
    return menuItemName;
  }

  public void setMenuItemName(String menuItemName) {
    this.menuItemName = menuItemName;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(BigDecimal unitPrice) {
    this.unitPrice = unitPrice;
    recalcSubtotal();
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
    recalcSubtotal();
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
  }
}

