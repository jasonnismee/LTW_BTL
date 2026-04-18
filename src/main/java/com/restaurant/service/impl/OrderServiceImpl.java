package com.restaurant.service.impl;

import com.restaurant.exception.BusinessException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.dto.CartItem;
import com.restaurant.model.dto.OrderDTO;
import com.restaurant.model.dto.VoucherResult;
import com.restaurant.model.entity.MenuItem;
import com.restaurant.model.entity.Order;
import com.restaurant.model.entity.OrderDetail;
import com.restaurant.model.entity.RestaurantTable;
import com.restaurant.model.entity.User;
import com.restaurant.model.entity.Voucher;
import com.restaurant.model.enums.MenuItemStatus;
import com.restaurant.model.enums.OrderStatus;
import com.restaurant.model.enums.OrderType;
import com.restaurant.model.enums.VoucherType;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderDetailRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.RestaurantTableRepository;
import com.restaurant.repository.VoucherRepository;
import com.restaurant.service.CartService;
import com.restaurant.service.OrderService;
import com.restaurant.service.RestaurantTableService;
import com.restaurant.service.UserService;
import com.restaurant.service.VoucherService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
  private final OrderRepository orderRepository;
  private final OrderDetailRepository orderDetailRepository;
  private final MenuItemRepository menuItemRepository;
  private final VoucherRepository voucherRepository;
  private final RestaurantTableRepository tableRepository;

  private final VoucherService voucherService;
  private final UserService userService;
  private final RestaurantTableService tableService;
  private final CartService cartService;

  public OrderServiceImpl(OrderRepository orderRepository,
                          OrderDetailRepository orderDetailRepository,
                          MenuItemRepository menuItemRepository,
                          VoucherRepository voucherRepository,
                          RestaurantTableRepository tableRepository,
                          VoucherService voucherService,
                          UserService userService,
                          RestaurantTableService tableService,
                          CartService cartService) {
    this.orderRepository = orderRepository;
    this.orderDetailRepository = orderDetailRepository;
    this.menuItemRepository = menuItemRepository;
    this.voucherRepository = voucherRepository;
    this.tableRepository = tableRepository;
    this.voucherService = voucherService;
    this.userService = userService;
    this.tableService = tableService;
    this.cartService = cartService;
  }

  @Override
  public Order createOnlineOrder(OrderDTO dto, Long userId, List<CartItem> cartItems, Long voucherId) {
    // CORE ONLINE flow:
    // - Validate địa chỉ giao hàng
    // - Tính totalPrice từ cartItems (session)
    // - Validate voucher theo rank user (BRIDGE 3)
    // - Lưu Order + OrderDetail, rồi clear cart (session)
    if (dto.getDeliveryAddress() == null || dto.getDeliveryAddress().isBlank()) {
      throw new BusinessException("Địa chỉ giao hàng không được để trống");
    }
    if (cartItems == null || cartItems.isEmpty()) {
      throw new BusinessException("Giỏ hàng đang trống");
    }

    User user = userService.findById(userId);
    BigDecimal totalPrice = calcTotal(cartItems);

    BigDecimal discountAmount = BigDecimal.ZERO;
    Voucher voucher = null;
    if (voucherId != null) {
      VoucherResult result = voucherService.validateAndApply(voucherId, totalPrice, user.getRank());
      if (!result.isValid()) {
        throw new BusinessException(result.getMessage());
      }
      discountAmount = result.getDiscountAmount();
      voucher = voucherRepository.findById(voucherId)
          .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher id=" + voucherId));
      voucherService.incrementUsedCount(voucherId);
    }

    BigDecimal finalPrice = totalPrice.subtract(discountAmount);
    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
      finalPrice = BigDecimal.ZERO;
    }

    Order order = new Order();
    order.setType(OrderType.ONLINE);
    order.setStatus(OrderStatus.PENDING);
    order.setUser(user);
    order.setTable(null);
    order.setVoucher(voucher);
    order.setPaymentMethod(dto.getPaymentMethod());
    order.setDeliveryAddress(dto.getDeliveryAddress().trim());
    order.setCustomerName(dto.getCustomerName());
    order.setCustomerPhone(dto.getCustomerPhone());
    order.setCustomerNote(dto.getCustomerNote());
    order.setTotalPrice(totalPrice);
    order.setDiscountAmount(discountAmount);
    order.setFinalPrice(finalPrice);
    Order saved = orderRepository.save(order);

    List<OrderDetail> details = createDetails(saved, cartItems);
    orderDetailRepository.saveAll(details);

    cartService.clearCart();
    return saved;
  }

  @Override
  public Order createOfflineOrder(OrderDTO dto, List<CartItem> cartItems, Long tableId, Long voucherId) {
    // CORE OFFLINE flow:
    // - Bàn được autoOccupyTable khi Staff thêm món đầu tiên vào bàn (POS).
    //   Vì vậy lúc thanh toán không gọi autoOccupyTable lần nữa để tránh lỗi OCCUPIED.
    // - Voucher OFFLINE: chỉ chấp nhận type=OFFLINE_ALL (không check rank)
    // - Lưu Order + OrderDetail
    if (cartItems == null || cartItems.isEmpty()) {
      throw new BusinessException("Chưa có món ăn để thanh toán");
    }
    RestaurantTable table = tableRepository.findById(tableId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn id=" + tableId));

    BigDecimal totalPrice = calcTotal(cartItems);
    BigDecimal discountAmount = BigDecimal.ZERO;
    Voucher voucher = null;
    if (voucherId != null) {
      voucher = voucherRepository.findById(voucherId)
          .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher id=" + voucherId));
      if (voucher.getType() != VoucherType.OFFLINE_ALL) {
        throw new BusinessException("Voucher không áp dụng cho thanh toán tại quầy");
      }
      VoucherResult result = voucherService.validateAndApply(voucherId, totalPrice, null);
      if (!result.isValid()) {
        throw new BusinessException(result.getMessage());
      }
      discountAmount = result.getDiscountAmount();
      voucherService.incrementUsedCount(voucherId);
    }

    BigDecimal finalPrice = totalPrice.subtract(discountAmount);
    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
      finalPrice = BigDecimal.ZERO;
    }

    Order order = new Order();
    order.setType(OrderType.OFFLINE);
    order.setStatus(OrderStatus.PENDING);
    order.setUser(null);
    order.setTable(table);
    order.setVoucher(voucher);
    order.setPaymentMethod(dto.getPaymentMethod());
    order.setCustomerName(dto.getCustomerName());
    order.setCustomerPhone(dto.getCustomerPhone());
    order.setCustomerNote(dto.getCustomerNote());
    order.setTotalPrice(totalPrice);
    order.setDiscountAmount(discountAmount);
    order.setFinalPrice(finalPrice);

    Order saved = orderRepository.save(order);
    List<OrderDetail> details = createDetails(saved, cartItems);
    orderDetailRepository.saveAll(details);

    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public Order findById(Long id) {
    return orderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order id=" + id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> getOrdersByUser(Long userId) {
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> getOrdersByUser(Long userId, Pageable pageable) {
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> getOnlineOrdersPending() {
    return orderRepository.findByTypeAndStatusIn(OrderType.ONLINE,
        List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.DELIVERING));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> getOnlineOrdersByStatus(OrderStatus status) {
    return orderRepository.findByTypeAndStatus(OrderType.ONLINE, status);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> getOfflineOrdersByTable(Long tableId) {
    return orderRepository.findByTypeAndTableIdOrderByCreatedAtDesc(OrderType.OFFLINE, tableId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderDetail> getOrderDetails(Long orderId) {
    return orderDetailRepository.findByOrderId(orderId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Order> getOpenOfflineOrderByTable(Long tableId) {
    return orderRepository.findFirstByTypeAndTableIdAndStatusNotInOrderByCreatedAtDesc(
        OrderType.OFFLINE,
        tableId,
        List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED)
    );
  }

  @Override
  public Order addItemToOpenOfflineOrder(Long tableId, Long menuItemId, Integer quantity) {
    if (quantity == null || quantity < 1) {
      throw new BusinessException("Số lượng phải >= 1");
    }
    Order order = getOrCreateOpenOfflineOrder(tableId);
    MenuItem menuItem = menuItemRepository.findById(menuItemId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy menu item id=" + menuItemId));
    if (menuItem.getStatus() != MenuItemStatus.AVAILABLE) {
      throw new BusinessException("Món ăn đã hết hàng: " + menuItem.getName());
    }

    OrderDetail detail = orderDetailRepository.findByOrderIdAndMenuItemId(order.getId(), menuItemId)
        .orElseGet(() -> {
          OrderDetail d = new OrderDetail();
          d.setOrder(order);
          d.setMenuItem(menuItem);
          d.setQuantity(0);
          d.setUnitPrice(menuItem.getPrice());
          d.setSubtotal(BigDecimal.ZERO);
          return d;
        });

    detail.setQuantity(detail.getQuantity() + quantity);
    detail.setUnitPrice(menuItem.getPrice());
    detail.setSubtotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
    orderDetailRepository.save(detail);

    return recalcOfflineOrderTotals(order);
  }

  @Override
  public Order updateItemQuantityInOpenOfflineOrder(Long tableId, Long menuItemId, Integer quantity) {
    if (quantity == null || quantity < 1) {
      throw new BusinessException("Số lượng phải >= 1");
    }
    Order order = getOpenOfflineOrderByTable(tableId)
        .orElseThrow(() -> new BusinessException("Bàn chưa có đơn OFFLINE đang mở"));

    OrderDetail detail = orderDetailRepository.findByOrderIdAndMenuItemId(order.getId(), menuItemId)
        .orElseThrow(() -> new BusinessException("Không tìm thấy món trong đơn"));

    detail.setQuantity(quantity);
    detail.setSubtotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
    orderDetailRepository.save(detail);

    return recalcOfflineOrderTotals(order);
  }

  @Override
  public Order removeItemFromOpenOfflineOrder(Long tableId, Long menuItemId) {
    Order order = getOpenOfflineOrderByTable(tableId)
        .orElseThrow(() -> new BusinessException("Bàn chưa có đơn OFFLINE đang mở"));

    OrderDetail detail = orderDetailRepository.findByOrderIdAndMenuItemId(order.getId(), menuItemId)
        .orElseThrow(() -> new BusinessException("Không tìm thấy món trong đơn"));
    orderDetailRepository.delete(detail);

    List<OrderDetail> remaining = orderDetailRepository.findByOrderId(order.getId());
    if (remaining.isEmpty()) {
      // Nếu xóa hết món => xóa đơn để bàn có thể trở lại EMPTY.
      orderRepository.delete(order);
      tableService.autoEmptyTable(tableId);
      return order;
    }

    return recalcOfflineOrderTotals(order);
  }

  @Override
  public Order checkoutOpenOfflineOrder(Long tableId, OrderDTO dto, Long voucherId, Long staffUserId) {
    Order order = getOpenOfflineOrderByTable(tableId)
        .orElseThrow(() -> new BusinessException("Bàn chưa có đơn OFFLINE đang mở"));

    // Recalc total trước khi áp voucher để tránh lệch dữ liệu do thao tác POS.
    order = recalcOfflineOrderTotals(order);

    BigDecimal totalPrice = order.getTotalPrice();
    BigDecimal discountAmount = BigDecimal.ZERO;
    Voucher voucher = null;
    if (voucherId != null) {
      voucher = voucherRepository.findById(voucherId)
          .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher id=" + voucherId));
      if (voucher.getType() != VoucherType.OFFLINE_ALL) {
        throw new BusinessException("Voucher không áp dụng cho thanh toán tại quầy");
      }
      VoucherResult result = voucherService.validateAndApply(voucherId, totalPrice, null);
      if (!result.isValid()) {
        throw new BusinessException(result.getMessage());
      }
      discountAmount = result.getDiscountAmount();
      voucherService.incrementUsedCount(voucherId);
    }

    BigDecimal finalPrice = totalPrice.subtract(discountAmount);
    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
      finalPrice = BigDecimal.ZERO;
    }

    order.setPaymentMethod(dto.getPaymentMethod());
    order.setCustomerName(dto.getCustomerName());
    order.setCustomerPhone(dto.getCustomerPhone());
    order.setCustomerNote(dto.getCustomerNote());
    order.setVoucher(voucher);
    order.setDiscountAmount(discountAmount);
    order.setFinalPrice(finalPrice);
    if (staffUserId != null) {
      order.setStaff(userService.findById(staffUserId));
    }
    return orderRepository.save(order);
  }

  @Override
  public Order updateOrderStatus(Long orderId, OrderStatus newStatus, Long actingStaffId) {
    // BRIDGE 1: Staff cập nhật status -> Customer tracking đọc từ DB thấy trạng thái mới nhất.
    // BRIDGE 1 (OFFLINE): khi COMPLETED -> autoEmptyTable để staff thấy bàn trống.
    // BRIDGE 3: khi ONLINE COMPLETED -> cộng dồn chi tiêu và thăng hạng.
    Order order = findById(orderId);
    order.setStatus(newStatus);
    if (actingStaffId != null) {
      order.setStaff(userService.findById(actingStaffId));
    }

    if (newStatus == OrderStatus.COMPLETED) {
      if (order.getType() == OrderType.ONLINE && order.getUser() != null) {
        userService.updateTotalSpendingAndRank(order.getUser().getId(), order.getFinalPrice());
      }
      if (order.getType() == OrderType.OFFLINE && order.getTable() != null) {
        tableService.autoEmptyTable(order.getTable().getId());
      }
    }

    if (newStatus == OrderStatus.CANCELLED && order.getVoucher() != null) {
      Voucher v = voucherRepository.findById(order.getVoucher().getId())
          .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher id=" + order.getVoucher().getId()));
      int used = v.getUsedCount() == null ? 0 : v.getUsedCount();
      v.setUsedCount(Math.max(0, used - 1));
      voucherRepository.save(v);
    }

    return orderRepository.save(order);
  }

  @Override
  public Order cancelOrder(Long orderId) {
    Order order = findById(orderId);
    if (order.getStatus() != OrderStatus.PENDING) {
      throw new BusinessException("Chỉ được hủy đơn khi trạng thái là PENDING");
    }
    return updateOrderStatus(orderId, OrderStatus.CANCELLED, null);
  }

  private BigDecimal calcTotal(List<CartItem> cartItems) {
    BigDecimal total = BigDecimal.ZERO;
    for (CartItem ci : cartItems) {
      if (ci.getSubtotal() == null) {
        ci.recalcSubtotal();
      }
      total = total.add(ci.getSubtotal());
    }
    return total;
  }

  private List<OrderDetail> createDetails(Order order, List<CartItem> cartItems) {
    List<OrderDetail> details = new ArrayList<>();
    for (CartItem ci : cartItems) {
      MenuItem menuItem = menuItemRepository.findById(ci.getMenuItemId())
          .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy menu item id=" + ci.getMenuItemId()));
      if (menuItem.getStatus() != MenuItemStatus.AVAILABLE) {
        throw new BusinessException("Món ăn đã hết hàng: " + menuItem.getName());
      }
      OrderDetail d = new OrderDetail();
      d.setOrder(order);
      d.setMenuItem(menuItem);
      d.setQuantity(ci.getQuantity());
      d.setUnitPrice(ci.getUnitPrice());
      d.setSubtotal(ci.getSubtotal());
      details.add(d);
    }
    return details;
  }

  private Order getOrCreateOpenOfflineOrder(Long tableId) {
    return getOpenOfflineOrderByTable(tableId).orElseGet(() -> {
      // Lúc thêm món đầu tiên, bàn phải chuyển OCCUPIED (automation bàn).
      tableService.autoOccupyTable(tableId);
      RestaurantTable table = tableRepository.findById(tableId)
          .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn id=" + tableId));

      Order order = new Order();
      order.setType(OrderType.OFFLINE);
      order.setStatus(OrderStatus.PENDING);
      order.setUser(null);
      order.setTable(table);
      order.setVoucher(null);
      order.setTotalPrice(BigDecimal.ZERO);
      order.setDiscountAmount(BigDecimal.ZERO);
      order.setFinalPrice(BigDecimal.ZERO);
      return orderRepository.save(order);
    });
  }

  private Order recalcOfflineOrderTotals(Order order) {
    // Nếu đơn đang mở mà đã từng áp voucher, thay đổi món sau đó sẽ làm voucher có thể không còn hợp lệ.
    // Để tránh sai tiền, khi recalc ta reset voucher/discount và yêu cầu chọn lại tại checkout.
    if (order.getType() == OrderType.OFFLINE && order.getStatus() == OrderStatus.PENDING && order.getVoucher() != null) {
      order.setVoucher(null);
      order.setDiscountAmount(BigDecimal.ZERO);
    }

    List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
    BigDecimal total = BigDecimal.ZERO;
    for (OrderDetail d : details) {
      total = total.add(d.getSubtotal() == null ? BigDecimal.ZERO : d.getSubtotal());
    }
    order.setTotalPrice(total);
    BigDecimal finalPrice = total.subtract(order.getDiscountAmount() == null ? BigDecimal.ZERO : order.getDiscountAmount());
    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
      finalPrice = BigDecimal.ZERO;
    }
    order.setFinalPrice(finalPrice);
    return orderRepository.save(order);
  }

  @Override
  public List<Order> findOrdersByTypeBetween(OrderType type, LocalDateTime from, LocalDateTime to) {
    return orderRepository.findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(type, from, to);
  }
}

