package com.restaurant.repository;

import com.restaurant.model.entity.Order;
import com.restaurant.model.enums.OrderStatus;
import com.restaurant.model.enums.OrderType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository extends JpaRepository<Order, Long> {
  List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

  Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  List<Order> findByTypeAndStatus(OrderType type, OrderStatus status);

  List<Order> findByTypeAndStatusIn(OrderType type, List<OrderStatus> statuses);

  List<Order> findByTypeAndTableIdOrderByCreatedAtDesc(OrderType type, Long tableId);

  Optional<Order> findFirstByTypeAndTableIdAndStatusNotInOrderByCreatedAtDesc(OrderType type, Long tableId, List<OrderStatus> excludedStatuses);

  boolean existsByTypeAndTableIdAndStatusNot(OrderType type, Long tableId, OrderStatus status);

  @Query("""
      select o
      from Order o
      where o.status = :status
        and o.createdAt < :beforeTime
      """)
  List<Order> findPendingBefore(@Param("status") OrderStatus status,
                                @Param("beforeTime") LocalDateTime beforeTime);

  @Query("""
      select count(o)
      from Order o
      where o.status = :status
      """)
  long countByStatus(@Param("status") OrderStatus status);

  @Query("""
      select coalesce(sum(o.finalPrice), 0)
      from Order o
      where o.status = com.restaurant.model.enums.OrderStatus.COMPLETED
        and o.createdAt between :from and :to
      """)
  BigDecimal sumRevenueCompletedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  @Query("""
      select count(o)
      from Order o
      where o.type = :type
        and o.createdAt between :from and :to
      """)
  long countOrdersByTypeBetween(@Param("type") OrderType type,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);
}

