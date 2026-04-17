package com.restaurant.service;

import com.restaurant.model.dto.CartItem;
import com.restaurant.model.dto.OrderDTO;
import com.restaurant.model.entity.Order;
import com.restaurant.model.entity.OrderDetail;
import com.restaurant.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

public interface OrderService {
  Order createOnlineOrder(OrderDTO dto, Long userId, List<CartItem> cartItems, Long voucherId);

  Order createOfflineOrder(OrderDTO dto, List<CartItem> cartItems, Long tableId, Long voucherId);

  Order findById(Long id);

  List<Order> getOrdersByUser(Long userId);

  Page<Order> getOrdersByUser(Long userId, Pageable pageable);

  List<Order> getOnlineOrdersPending();

  List<Order> getOnlineOrdersByStatus(OrderStatus status);

  List<Order> getOfflineOrdersByTable(Long tableId);

  List<OrderDetail> getOrderDetails(Long orderId);

  Optional<Order> getOpenOfflineOrderByTable(Long tableId);

  Order addItemToOpenOfflineOrder(Long tableId, Long menuItemId, Integer quantity);

  Order updateItemQuantityInOpenOfflineOrder(Long tableId, Long menuItemId, Integer quantity);

  Order removeItemFromOpenOfflineOrder(Long tableId, Long menuItemId);

  Order checkoutOpenOfflineOrder(Long tableId, OrderDTO dto, Long voucherId);

  Order updateOrderStatus(Long orderId, OrderStatus newStatus);

  Order cancelOrder(Long orderId);
}

