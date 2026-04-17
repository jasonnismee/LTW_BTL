package com.restaurant.util;

import com.restaurant.model.entity.Order;
import com.restaurant.model.enums.OrderStatus;
import com.restaurant.repository.OrderRepository;
import com.restaurant.service.OrderService;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderScheduler {
  private static final Logger log = LoggerFactory.getLogger(OrderScheduler.class);

  private final OrderRepository orderRepository;
  private final OrderService orderService;

  public OrderScheduler(OrderRepository orderRepository, OrderService orderService) {
    this.orderRepository = orderRepository;
    this.orderService = orderService;
  }

  @Scheduled(fixedRate = 300000)
  public void autoCancelPendingOrders() {
    LocalDateTime before = LocalDateTime.now().minusMinutes(30);
    List<Order> expired = orderRepository.findPendingBefore(OrderStatus.PENDING, before);
    for (Order o : expired) {
      orderService.updateOrderStatus(o.getId(), OrderStatus.CANCELLED);
    }
    if (!expired.isEmpty()) {
      log.info("Auto-cancelled {} pending orders older than 30 minutes", expired.size());
    }
  }
}

