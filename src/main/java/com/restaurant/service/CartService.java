package com.restaurant.service;

import com.restaurant.model.dto.CartItem;
import java.math.BigDecimal;
import java.util.List;

public interface CartService {
  CartItem addToCart(Long menuItemId, Integer quantity);

  CartItem updateQuantity(Long menuItemId, Integer quantity);

  void removeItem(Long menuItemId);

  void clearCart();

  List<CartItem> getCart();

  BigDecimal getCartTotal();
}

