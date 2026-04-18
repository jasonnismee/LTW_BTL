package com.restaurant.controller.admin;

import com.restaurant.model.entity.Order;
import com.restaurant.model.enums.OrderType;
import com.restaurant.service.OrderService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
  private final OrderService orderService;

  public AdminOrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @ModelAttribute("activeMenu")
  public String activeMenu() {
    return "orders";
  }

  @GetMapping
  public String manage(@RequestParam(value = "period", defaultValue = "day") String period,
                       @RequestParam(value = "refDate", required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate refDate,
                       @RequestParam(value = "tab", defaultValue = "offline") String tab,
                       Model model) {
    LocalDate ref = refDate != null ? refDate : LocalDate.now();
    String p = normalizePeriod(period);
    LocalDateTime from = rangeStart(p, ref);
    LocalDateTime to = rangeEnd(p, ref);

    List<Order> offline = orderService.findOrdersByTypeBetween(OrderType.OFFLINE, from, to);
    List<Order> online = orderService.findOrdersByTypeBetween(OrderType.ONLINE, from, to);

    model.addAttribute("offlineOrders", offline);
    model.addAttribute("onlineOrders", online);
    model.addAttribute("period", p);
    model.addAttribute("refDate", ref);
    model.addAttribute("activeTab", "online".equalsIgnoreCase(tab) ? "online" : "offline");
    model.addAttribute("rangeLabel", formatRangeLabel(p, ref, from, to));
    return "admin/order-manage";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable("id") Long id, Model model) {
    model.addAttribute("order", orderService.findById(id));
    model.addAttribute("details", orderService.getOrderDetails(id));
    return "admin/order-detail";
  }

  private static String normalizePeriod(String period) {
    if ("week".equalsIgnoreCase(period)) {
      return "week";
    }
    if ("month".equalsIgnoreCase(period)) {
      return "month";
    }
    return "day";
  }

  private static LocalDateTime rangeStart(String period, LocalDate ref) {
    if ("week".equals(period)) {
      return ref.with(DayOfWeek.MONDAY).atStartOfDay();
    }
    if ("month".equals(period)) {
      return ref.withDayOfMonth(1).atStartOfDay();
    }
    return ref.atStartOfDay();
  }

  private static LocalDateTime rangeEnd(String period, LocalDate ref) {
    if ("week".equals(period)) {
      return ref.with(DayOfWeek.MONDAY).plusDays(6).atTime(LocalTime.MAX);
    }
    if ("month".equals(period)) {
      return ref.withDayOfMonth(ref.lengthOfMonth()).atTime(LocalTime.MAX);
    }
    return ref.atTime(LocalTime.MAX);
  }

  private static String formatRangeLabel(String period, LocalDate ref, LocalDateTime from, LocalDateTime to) {
    DateTimeFormatter d = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    if ("week".equals(period)) {
      return "Tuần: " + from.toLocalDate().format(d) + " – " + to.toLocalDate().format(d);
    }
    if ("month".equals(period)) {
      return "Tháng " + ref.getMonthValue() + "/" + ref.getYear();
    }
    return "Ngày " + ref.format(d);
  }
}
