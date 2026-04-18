package com.restaurant.controller.staff;

import com.restaurant.exception.BusinessException;
import com.restaurant.model.dto.OrderDTO;
import com.restaurant.model.dto.VoucherResult;
import com.restaurant.model.entity.Order;
import com.restaurant.model.entity.OrderDetail;
import com.restaurant.model.entity.Voucher;
import com.restaurant.service.MenuItemService;
import com.restaurant.service.OrderService;
import com.restaurant.service.RestaurantTableService;
import com.restaurant.service.VoucherService;
import com.restaurant.util.SecurityUtils;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/pos")
@PreAuthorize("hasRole('STAFF')")
public class PosController {
  private final RestaurantTableService tableService;
  private final MenuItemService menuItemService;
  private final VoucherService voucherService;
  private final OrderService orderService;

  public PosController(RestaurantTableService tableService,
                       MenuItemService menuItemService,
                       VoucherService voucherService,
                       OrderService orderService) {
    this.tableService = tableService;
    this.menuItemService = menuItemService;
    this.voucherService = voucherService;
    this.orderService = orderService;
  }

  @org.springframework.web.bind.annotation.ModelAttribute("activeMenu")
  public String activeMenu() {
    return "pos";
  }

  @GetMapping({"", "/"})
  public String pos(@RequestParam(value = "q", required = false) String q, Model model) {
    model.addAttribute("tables", tableService.getAll());
    model.addAttribute("menuItems", (q == null || q.isBlank()) ? menuItemService.getAllAvailable() : menuItemService.searchByName(q));
    model.addAttribute("q", q);
    return "staff/pos";
  }

  @GetMapping("/table/{id}")
  public String table(@PathVariable("id") Long tableId,
                      @RequestParam(value = "q", required = false) String q,
                      Model model) {
    model.addAttribute("tables", tableService.getAll());
    model.addAttribute("menuItems", (q == null || q.isBlank()) ? menuItemService.getAllAvailable() : menuItemService.searchByName(q));
    model.addAttribute("q", q);
    model.addAttribute("tableId", tableId);
    Order openOrder = orderService.getOpenOfflineOrderByTable(tableId).orElse(null);
    List<OrderDetail> details = (openOrder == null) ? List.of() : orderService.getOrderDetails(openOrder.getId());
    model.addAttribute("order", openOrder);
    model.addAttribute("details", details);
    model.addAttribute("totalPrice", openOrder == null ? BigDecimal.ZERO : openOrder.getTotalPrice());
    return "staff/pos-table";
  }

  @PostMapping("/table/{id}/add-item")
  public String addItem(@PathVariable("id") Long tableId,
                        @RequestParam("menuItemId") Long menuItemId,
                        @RequestParam("quantity") Integer quantity,
                        RedirectAttributes redirectAttributes) {
    orderService.addItemToOpenOfflineOrder(tableId, menuItemId, quantity);
    redirectAttributes.addFlashAttribute("success", "Đã thêm món vào bàn");
    return "redirect:/staff/pos/table/" + tableId;
  }

  @PostMapping("/table/{id}/update-item")
  public String updateItem(@PathVariable("id") Long tableId,
                           @RequestParam("menuItemId") Long menuItemId,
                           @RequestParam("quantity") Integer quantity,
                           RedirectAttributes redirectAttributes) {
    orderService.updateItemQuantityInOpenOfflineOrder(tableId, menuItemId, quantity);
    redirectAttributes.addFlashAttribute("success", "Đã cập nhật số lượng");
    return "redirect:/staff/pos/table/" + tableId;
  }

  @PostMapping("/table/{id}/remove-item")
  public String removeItem(@PathVariable("id") Long tableId,
                           @RequestParam("menuItemId") Long menuItemId,
                           RedirectAttributes redirectAttributes) {
    orderService.removeItemFromOpenOfflineOrder(tableId, menuItemId);
    redirectAttributes.addFlashAttribute("success", "Đã xóa món");
    return "redirect:/staff/pos/table/" + tableId;
  }

  @GetMapping("/table/{id}/checkout")
  public String checkout(@PathVariable("id") Long tableId, Model model) {
    Order openOrder = orderService.getOpenOfflineOrderByTable(tableId)
        .orElseThrow(() -> new BusinessException("Bàn chưa có đơn OFFLINE đang mở"));
    List<OrderDetail> details = orderService.getOrderDetails(openOrder.getId());
    BigDecimal total = openOrder.getTotalPrice();
    List<Voucher> vouchers = voucherService.getOfflineVouchers();
    Long bestVoucherId = suggestBestVoucher(vouchers, total);

    model.addAttribute("tableId", tableId);
    model.addAttribute("posOrder", openOrder);
    model.addAttribute("details", details);
    model.addAttribute("totalPrice", total);
    model.addAttribute("vouchers", vouchers);
    model.addAttribute("bestVoucherId", bestVoucherId);
    model.addAttribute("orderForm", new OrderDTO());
    return "staff/pos-checkout";
  }

  @PostMapping("/table/{id}/pay")
  public String pay(@PathVariable("id") Long tableId,
                    OrderDTO orderForm,
                    @RequestParam(value = "voucherId", required = false) Long voucherId,
                    RedirectAttributes redirectAttributes) {
    Long staffId = SecurityUtils.currentUserId();
    Order checkedOut = orderService.checkoutOpenOfflineOrder(tableId, orderForm, voucherId, staffId);
    Order completed = orderService.updateOrderStatus(checkedOut.getId(), com.restaurant.model.enums.OrderStatus.COMPLETED, staffId);

    redirectAttributes.addFlashAttribute("success", "Thanh toán thành công");
    return "redirect:/staff/pos/invoice?orderId=" + completed.getId();
  }

  @GetMapping("/invoice")
  public String invoice(@RequestParam("orderId") Long orderId, Model model) {
    model.addAttribute("order", orderService.findById(orderId));
    model.addAttribute("details", orderService.getOrderDetails(orderId));
    return "staff/invoice";
  }

  private Long suggestBestVoucher(List<Voucher> vouchers, BigDecimal total) {
    if (vouchers == null || vouchers.isEmpty() || total == null) return null;
    return vouchers.stream()
        .map(v -> new Object[] {v.getId(), voucherService.validateAndApply(v.getId(), total, null)})
        .filter(arr -> ((VoucherResult) arr[1]).isValid())
        .max(Comparator.comparing(arr -> ((VoucherResult) arr[1]).getDiscountAmount()))
        .map(arr -> (Long) arr[0])
        .orElse(null);
  }
}

