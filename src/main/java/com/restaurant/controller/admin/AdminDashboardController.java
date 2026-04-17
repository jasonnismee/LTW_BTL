package com.restaurant.controller.admin;

import com.restaurant.model.enums.OrderStatus;
import com.restaurant.model.enums.OrderType;
import com.restaurant.model.enums.TableStatus;
import com.restaurant.repository.OrderDetailRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.RestaurantTableRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
  private final OrderRepository orderRepository;
  private final OrderDetailRepository orderDetailRepository;
  private final RestaurantTableRepository tableRepository;

  public AdminDashboardController(OrderRepository orderRepository,
                                  OrderDetailRepository orderDetailRepository,
                                  RestaurantTableRepository tableRepository) {
    this.orderRepository = orderRepository;
    this.orderDetailRepository = orderDetailRepository;
    this.tableRepository = tableRepository;
  }

  @ModelAttribute("activeMenu")
  public String activeMenu() {
    return "dashboard";
  }

  @GetMapping
  public String dashboard(Model model) {
    LocalDate today = LocalDate.now();
    LocalDateTime dayStart = today.atStartOfDay();
    LocalDateTime dayEnd = today.atTime(LocalTime.MAX);

    LocalDate monday = today.with(DayOfWeek.MONDAY);
    LocalDateTime weekStart = monday.atStartOfDay();
    LocalDateTime weekEnd = today.atTime(LocalTime.MAX);

    LocalDate firstDayOfMonth = today.withDayOfMonth(1);
    LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
    LocalDateTime monthEnd = today.atTime(LocalTime.MAX);

    BigDecimal revenueDay = orderRepository.sumRevenueCompletedBetween(dayStart, dayEnd);
    BigDecimal revenueWeek = orderRepository.sumRevenueCompletedBetween(weekStart, weekEnd);
    BigDecimal revenueMonth = orderRepository.sumRevenueCompletedBetween(monthStart, monthEnd);

    long onlineToday = orderRepository.countOrdersByTypeBetween(OrderType.ONLINE, dayStart, dayEnd);
    long offlineToday = orderRepository.countOrdersByTypeBetween(OrderType.OFFLINE, dayStart, dayEnd);

    long occupiedTables = tableRepository.countByStatus(TableStatus.OCCUPIED);
    long pendingCount = orderRepository.countByStatus(OrderStatus.PENDING);

    List<OrderDetailRepository.TopMenuItem> topItems = orderDetailRepository.findTopSelling(PageRequest.of(0, 5));

    model.addAttribute("revenueDay", revenueDay);
    model.addAttribute("revenueWeek", revenueWeek);
    model.addAttribute("revenueMonth", revenueMonth);
    model.addAttribute("onlineToday", onlineToday);
    model.addAttribute("offlineToday", offlineToday);
    model.addAttribute("occupiedTables", occupiedTables);
    model.addAttribute("pendingCount", pendingCount);
    model.addAttribute("topItems", topItems);

    List<String> chartLabels = new ArrayList<>();
    List<BigDecimal> chartValues = new ArrayList<>();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
    for (int i = 6; i >= 0; i--) {
      LocalDate d = today.minusDays(i);
      LocalDateTime from = d.atStartOfDay();
      LocalDateTime to = d.atTime(LocalTime.MAX);
      chartLabels.add(d.format(fmt));
      chartValues.add(orderRepository.sumRevenueCompletedBetween(from, to));
    }
    model.addAttribute("chartLabels", chartLabels);
    model.addAttribute("chartValues", chartValues);

    return "admin/dashboard";
  }
}

