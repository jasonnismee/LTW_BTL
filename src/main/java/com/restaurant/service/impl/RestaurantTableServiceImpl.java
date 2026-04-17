package com.restaurant.service.impl;

import com.restaurant.exception.BusinessException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.entity.RestaurantTable;
import com.restaurant.model.enums.OrderStatus;
import com.restaurant.model.enums.OrderType;
import com.restaurant.model.enums.TableStatus;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.RestaurantTableRepository;
import com.restaurant.service.RestaurantTableService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RestaurantTableServiceImpl implements RestaurantTableService {
  private final RestaurantTableRepository tableRepository;
  private final OrderRepository orderRepository;

  public RestaurantTableServiceImpl(RestaurantTableRepository tableRepository, OrderRepository orderRepository) {
    this.tableRepository = tableRepository;
    this.orderRepository = orderRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<RestaurantTable> getAll() {
    return tableRepository.findAllByOrderByTableNumberAsc();
  }

  @Override
  @Transactional(readOnly = true)
  public List<RestaurantTable> getEmptyTables() {
    return tableRepository.findByStatusOrderByTableNumberAsc(TableStatus.EMPTY);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RestaurantTable> getOccupiedTables() {
    return tableRepository.findByStatusOrderByTableNumberAsc(TableStatus.OCCUPIED);
  }

  @Override
  public void autoOccupyTable(Long tableId) {
    // Automation (bàn):
    // - Khi Staff thêm món đầu tiên vào bàn, nếu bàn đang EMPTY thì chuyển OCCUPIED.
    // - Nếu đã OCCUPIED thì chặn để tránh 2 ca phục vụ trùng bàn.
    RestaurantTable table = tableRepository.findById(tableId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn id=" + tableId));

    if (table.getStatus() == TableStatus.EMPTY) {
      table.setStatus(TableStatus.OCCUPIED);
      tableRepository.save(table);
      return;
    }
    throw new BusinessException("Bàn này đang có người sử dụng");
  }

  @Override
  public void autoEmptyTable(Long tableId) {
    // Automation (bàn):
    // - Khi đơn OFFLINE được COMPLETED, thử trả bàn về EMPTY.
    // - Nhưng nếu còn bất kỳ đơn OFFLINE nào chưa COMPLETED trên bàn đó thì vẫn giữ OCCUPIED.
    RestaurantTable table = tableRepository.findById(tableId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn id=" + tableId));

    boolean hasOpenOrders = orderRepository.existsByTypeAndTableIdAndStatusNot(OrderType.OFFLINE, tableId, OrderStatus.COMPLETED);
    if (hasOpenOrders) {
      return;
    }
    table.setStatus(TableStatus.EMPTY);
    tableRepository.save(table);
  }
}

