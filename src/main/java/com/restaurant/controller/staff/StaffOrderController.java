package com.restaurant.controller.staff;

import com.restaurant.exception.BusinessException;
import com.restaurant.model.entity.Order;
import com.restaurant.model.enums.OrderStatus;
import com.restaurant.service.OrderService;
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
@RequestMapping("/staff/orders")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
public class StaffOrderController {
  private final OrderService orderService;

  public StaffOrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @org.springframework.web.bind.annotation.ModelAttribute("activeMenu")
  public String activeMenu() {
    return "onlineOrders";
  }

  @GetMapping("/online")
  public String onlineOrders(Model model) {
    List<Order> orders = orderService.getOnlineOrdersPending();
    model.addAttribute("orders", orders);
    return "staff/online-orders";
  }

  @GetMapping("/{id}/detail")
  public String detail(@PathVariable("id") Long id, Model model) {
    model.addAttribute("order", orderService.findById(id));
    model.addAttribute("details", orderService.getOrderDetails(id));
    return "staff/order-detail";
  }

  @PostMapping("/{id}/update-status")
  public String updateStatus(@PathVariable("id") Long id,
                             @RequestParam("newStatus") OrderStatus newStatus,
                             RedirectAttributes redirectAttributes) {
    Order current = orderService.findById(id);
    if (newStatus == OrderStatus.CANCELLED) {
      orderService.updateOrderStatus(id, OrderStatus.CANCELLED);
      redirectAttributes.addFlashAttribute("success", "Đã hủy đơn");
      return "redirect:/staff/orders/online";
    }
    if (!isForwardStep(current.getStatus(), newStatus)) {
      throw new BusinessException("Không cho phép cập nhật trạng thái quay ngược");
    }
    orderService.updateOrderStatus(id, newStatus);
    redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái");
    return "redirect:/staff/orders/online";
  }

  private boolean isForwardStep(OrderStatus current, OrderStatus next) {
    if (current == OrderStatus.PENDING && next == OrderStatus.PREPARING) return true;
    if (current == OrderStatus.PREPARING && next == OrderStatus.DELIVERING) return true;
    if (current == OrderStatus.DELIVERING && next == OrderStatus.COMPLETED) return true;
    return false;
  }
}

