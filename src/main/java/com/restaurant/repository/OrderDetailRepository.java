package com.restaurant.repository;

import com.restaurant.model.entity.OrderDetail;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
  List<OrderDetail> findByOrderId(Long orderId);

  Optional<OrderDetail> findByOrderIdAndMenuItemId(Long orderId, Long menuItemId);

  interface TopMenuItem {
    Long getMenuItemId();

    String getMenuItemName();

    Long getTotalQuantity();
  }

  @Query("""
      select d.menuItem.id as menuItemId,
             d.menuItem.name as menuItemName,
             sum(d.quantity) as totalQuantity
      from OrderDetail d
      where d.order.status = com.restaurant.model.enums.OrderStatus.COMPLETED
      group by d.menuItem.id, d.menuItem.name
      order by sum(d.quantity) desc
      """)
  List<TopMenuItem> findTopSelling(Pageable pageable);

  @Query("""
      select sum(d.subtotal)
      from OrderDetail d
      where d.order.id = :orderId
      """)
  java.math.BigDecimal sumSubtotalByOrderId(@Param("orderId") Long orderId);
}

