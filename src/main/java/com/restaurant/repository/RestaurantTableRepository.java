package com.restaurant.repository;

import com.restaurant.model.entity.RestaurantTable;
import com.restaurant.model.enums.TableStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
  List<RestaurantTable> findByStatusOrderByTableNumberAsc(TableStatus status);

  List<RestaurantTable> findAllByOrderByTableNumberAsc();

  long countByStatus(TableStatus status);
}

