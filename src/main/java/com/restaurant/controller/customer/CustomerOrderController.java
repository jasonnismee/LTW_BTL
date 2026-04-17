package com.restaurant.controller.customer;

import com.restaurant.model.entity.Order;
import com.restaurant.service.OrderService;
import com.restaurant.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/customer/orders")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerOrderController {
  private final OrderService orderService;

  public CustomerOrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  public String history(@RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        Model model) {
    Long userId = SecurityUtils.currentUserId();
    int safeSize = Math.min(Math.max(size, 5), 50);
    int safePage = Math.max(page, 0);
    Page<Order> ordersPage = orderService.getOrdersByUser(
        userId,
        PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
    );
    model.addAttribute("ordersPage", ordersPage);
    model.addAttribute("size", safeSize);
    return "customer/history";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable("id") Long id, Model model) {
    Order order = orderService.findById(id);
    model.addAttribute("order", order);
    model.addAttribute("details", orderService.getOrderDetails(id));
    return "customer/order-detail";
  }

  @PostMapping("/{id}/cancel")
  public String cancel(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    orderService.cancelOrder(id);
    redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng");
    return "redirect:/customer/orders";
  }
}

