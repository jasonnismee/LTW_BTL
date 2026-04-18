package com.restaurant.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.restaurant.exception.BusinessException;
import com.restaurant.model.dto.CartItem;
import com.restaurant.model.dto.OrderDTO;
import com.restaurant.model.dto.VoucherResult;
import com.restaurant.model.entity.MenuItem;
import com.restaurant.model.entity.Order;
import com.restaurant.model.entity.RestaurantTable;
import com.restaurant.model.entity.User;
import com.restaurant.model.entity.Voucher;
import com.restaurant.model.enums.MenuItemStatus;
import com.restaurant.model.enums.OrderStatus;
import com.restaurant.model.enums.OrderType;
import com.restaurant.model.enums.PaymentMethod;
import com.restaurant.model.enums.UserRank;
import com.restaurant.model.enums.VoucherType;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderDetailRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.RestaurantTableRepository;
import com.restaurant.repository.VoucherRepository;
import com.restaurant.service.impl.OrderServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
  @Mock private OrderRepository orderRepository;
  @Mock private OrderDetailRepository orderDetailRepository;
  @Mock private MenuItemRepository menuItemRepository;
  @Mock private VoucherRepository voucherRepository;
  @Mock private RestaurantTableRepository tableRepository;
  @Mock private VoucherService voucherService;
  @Mock private UserService userService;
  @Mock private RestaurantTableService tableService;
  @Mock private CartService cartService;

  private OrderServiceImpl orderService;

  @BeforeEach
  void setUp() {
    orderService = new OrderServiceImpl(
        orderRepository,
        orderDetailRepository,
        menuItemRepository,
        voucherRepository,
        tableRepository,
        voucherService,
        userService,
        tableService,
        cartService
    );
  }

  @Test
  void testCreateOnlineOrder_Success_SavesOrderAndDetails() {
    User user = new User();
    user.setId(10L);
    user.setRank(UserRank.BRONZE);

    OrderDTO dto = new OrderDTO();
    dto.setPaymentMethod(PaymentMethod.CASH);
    dto.setDeliveryAddress("123 Street");

    List<CartItem> cart = List.of(new CartItem(1L, "Item 1", new BigDecimal("50000"), 2));

    MenuItem menuItem = new MenuItem();
    menuItem.setId(1L);
    menuItem.setName("Item 1");
    menuItem.setPrice(new BigDecimal("50000"));
    menuItem.setStatus(MenuItemStatus.AVAILABLE);

    Voucher voucher = new Voucher();
    voucher.setId(99L);
    voucher.setType(VoucherType.ONLINE_RANK_ONLY);

    when(userService.findById(10L)).thenReturn(user);
    when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
    when(voucherService.validateAndApply(99L, new BigDecimal("100000"), UserRank.BRONZE))
        .thenReturn(new VoucherResult(true, new BigDecimal("10000.00"), new BigDecimal("90000.00"), "OK"));
    when(voucherRepository.findById(99L)).thenReturn(Optional.of(voucher));

    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
      Order o = inv.getArgument(0);
      o.setId(123L);
      return o;
    });

    Order created = orderService.createOnlineOrder(dto, 10L, cart, 99L);

    assertNotNull(created.getId());
    assertEquals(OrderType.ONLINE, created.getType());
    assertEquals(OrderStatus.PENDING, created.getStatus());
    verify(orderRepository, times(1)).save(any(Order.class));
    verify(orderDetailRepository, times(1)).saveAll(anyList());
    verify(cartService, times(1)).clearCart();
    verify(voucherService, times(1)).incrementUsedCount(99L);
  }

  @Test
  void testCreateOnlineOrder_InvalidVoucher_ThrowsException() {
    User user = new User();
    user.setId(10L);
    user.setRank(UserRank.BRONZE);

    OrderDTO dto = new OrderDTO();
    dto.setDeliveryAddress("123 Street");

    List<CartItem> cart = List.of(new CartItem(1L, "Item 1", new BigDecimal("50000"), 2));
    MenuItem menuItem = new MenuItem();
    menuItem.setId(1L);
    menuItem.setName("Item 1");
    menuItem.setPrice(new BigDecimal("50000"));
    menuItem.setStatus(MenuItemStatus.AVAILABLE);

    when(userService.findById(10L)).thenReturn(user);
    when(voucherService.validateAndApply(any(), any(), any())).thenReturn(new VoucherResult(false, BigDecimal.ZERO, BigDecimal.ZERO, "Invalid"));

    assertThrows(BusinessException.class, () -> orderService.createOnlineOrder(dto, 10L, cart, 99L));
  }

  @Test
  void testUpdateOrderStatus_ToCompleted_CallsUserServiceRankUpdate() {
    User user = new User();
    user.setId(10L);

    Order order = new Order();
    order.setId(1L);
    order.setType(OrderType.ONLINE);
    order.setStatus(OrderStatus.PENDING);
    order.setUser(user);
    order.setFinalPrice(new BigDecimal("123000"));

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    Order updated = orderService.updateOrderStatus(1L, OrderStatus.COMPLETED, null);
    assertEquals(OrderStatus.COMPLETED, updated.getStatus());
    verify(userService, times(1)).updateTotalSpendingAndRank(10L, new BigDecimal("123000"));
  }

  @Test
  void testUpdateOrderStatus_OfflineOrder_ToCompleted_CallsAutoEmptyTable() {
    RestaurantTable t = new RestaurantTable();
    t.setId(5L);

    Order order = new Order();
    order.setId(2L);
    order.setType(OrderType.OFFLINE);
    order.setStatus(OrderStatus.PENDING);
    order.setTable(t);
    order.setFinalPrice(new BigDecimal("50000"));

    when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    orderService.updateOrderStatus(2L, OrderStatus.COMPLETED, null);
    verify(tableService, times(1)).autoEmptyTable(5L);
  }
}

