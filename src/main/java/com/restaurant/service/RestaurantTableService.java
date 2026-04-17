package com.restaurant.service;

import com.restaurant.model.entity.RestaurantTable;
import java.util.List;

public interface RestaurantTableService {
  List<RestaurantTable> getAll();

  List<RestaurantTable> getEmptyTables();

  List<RestaurantTable> getOccupiedTables();

  void autoOccupyTable(Long tableId);

  void autoEmptyTable(Long tableId);
}

