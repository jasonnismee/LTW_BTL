package com.restaurant.service.impl;

import com.restaurant.exception.BusinessException;
import com.restaurant.model.dto.CartItem;
import com.restaurant.model.entity.MenuItem;
import com.restaurant.model.enums.MenuItemStatus;
import com.restaurant.service.CartService;
import com.restaurant.service.MenuItemService;
import com.restaurant.util.AppConstants;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {
  private final HttpSession session;
  private final MenuItemService menuItemService;

  public CartServiceImpl(HttpSession session, MenuItemService menuItemService) {
    this.session = session;
    this.menuItemService = menuItemService;
  }

  @Override
  public CartItem addToCart(Long menuItemId, Integer quantity) {
    validateQuantity(quantity);
    MenuItem item = menuItemService.getById(menuItemId);
    if (item.getStatus() != MenuItemStatus.AVAILABLE) {
      throw new BusinessException("Món ăn đã hết hàng");
    }

    List<CartItem> cart = getOrInitCart();
    for (CartItem ci : cart) {
      if (ci.getMenuItemId().equals(menuItemId)) {
        ci.setQuantity(ci.getQuantity() + quantity);
        store(cart);
        return ci;
      }
    }
    CartItem created = new CartItem(item.getId(), item.getName(), item.getPrice(), quantity);
    cart.add(created);
    cart.sort(Comparator.comparing(CartItem::getMenuItemName, String.CASE_INSENSITIVE_ORDER));
    store(cart);
    return created;
  }

  @Override
  public CartItem updateQuantity(Long menuItemId, Integer quantity) {
    validateQuantity(quantity);
    List<CartItem> cart = getOrInitCart();
    for (CartItem ci : cart) {
      if (ci.getMenuItemId().equals(menuItemId)) {
        ci.setQuantity(quantity);
        store(cart);
        return ci;
      }
    }
    throw new BusinessException("Không tìm thấy món trong giỏ hàng");
  }

  @Override
  public void removeItem(Long menuItemId) {
    List<CartItem> cart = getOrInitCart();
    cart.removeIf(ci -> ci.getMenuItemId().equals(menuItemId));
    store(cart);
  }

  @Override
  public void clearCart() {
    session.removeAttribute(AppConstants.SessionKeys.CART);
  }

  @Override
  public List<CartItem> getCart() {
    return new ArrayList<>(getOrInitCart());
  }

  @Override
  public BigDecimal getCartTotal() {
    BigDecimal total = BigDecimal.ZERO;
    for (CartItem ci : getOrInitCart()) {
      total = total.add(ci.getSubtotal() == null ? BigDecimal.ZERO : ci.getSubtotal());
    }
    return total;
  }

  @SuppressWarnings("unchecked")
  private List<CartItem> getOrInitCart() {
    Object obj = session.getAttribute(AppConstants.SessionKeys.CART);
    if (obj instanceof List<?>) {
      return (List<CartItem>) obj;
    }
    List<CartItem> cart = new ArrayList<>();
    store(cart);
    return cart;
  }

  private void store(List<CartItem> cart) {
    session.setAttribute(AppConstants.SessionKeys.CART, cart);
  }

  private void validateQuantity(Integer quantity) {
    if (quantity == null || quantity < 1) {
      throw new BusinessException("Số lượng phải >= 1");
    }
  }
}

