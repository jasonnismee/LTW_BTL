package com.restaurant.controller.customer;

import com.restaurant.model.dto.OrderDTO;
import com.restaurant.model.dto.VoucherResult;
import com.restaurant.model.entity.Order;
import com.restaurant.model.entity.User;
import com.restaurant.model.entity.Voucher;
import com.restaurant.service.CartService;
import com.restaurant.service.OrderService;
import com.restaurant.service.UserService;
import com.restaurant.service.VoucherService;
import com.restaurant.util.SecurityUtils;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer/checkout")
@PreAuthorize("hasRole('CUSTOMER')")
public class CheckoutController {
  private final CartService cartService;
  private final VoucherService voucherService;
  private final OrderService orderService;
  private final UserService userService;

  public CheckoutController(CartService cartService, VoucherService voucherService, OrderService orderService, UserService userService) {
    this.cartService = cartService;
    this.voucherService = voucherService;
    this.orderService = orderService;
    this.userService = userService;
  }

  @GetMapping
  public String form(Model model) {
    Long userId = SecurityUtils.currentUserId();
    User user = userService.findById(userId);

    BigDecimal total = cartService.getCartTotal();
    List<Voucher> vouchers = voucherService.getOnlineVouchersForRank(user.getRank());
    Long bestVoucherId = suggestBestVoucher(vouchers, total, user.getRank());

    if (!model.containsAttribute("order")) {
      model.addAttribute("order", new OrderDTO());
    }
    model.addAttribute("cartItems", cartService.getCart());
    model.addAttribute("totalPrice", total);
    model.addAttribute("vouchers", vouchers);
    model.addAttribute("bestVoucherId", bestVoucherId);
    model.addAttribute("rank", user.getRank());
    return "customer/checkout";
  }

  @GetMapping("/preview")
  @ResponseBody
  public ResponseEntity<VoucherResult> preview(@RequestParam(value = "voucherId", required = false) Long voucherId) {
    Long userId = SecurityUtils.currentUserId();
    User user = userService.findById(userId);
    BigDecimal total = cartService.getCartTotal();
    if (voucherId == null) {
      return ResponseEntity.ok(new VoucherResult(true, BigDecimal.ZERO, total, "Không áp dụng voucher"));
    }
    VoucherResult result = voucherService.validateAndApply(voucherId, total, user.getRank());
    return ResponseEntity.ok(result);
  }

  @PostMapping
  public String checkout(@Valid OrderDTO order, BindingResult bindingResult,
                         @RequestParam(value = "voucherId", required = false) Long voucherId,
                         RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.order", bindingResult);
      redirectAttributes.addFlashAttribute("order", order);
      redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin");
      return "redirect:/customer/checkout";
    }

    Long userId = SecurityUtils.currentUserId();
    Order created = orderService.createOnlineOrder(order, userId, cartService.getCart(), voucherId);
    redirectAttributes.addFlashAttribute("orderId", created.getId());
    return "redirect:/customer/checkout/success?orderId=" + created.getId();
  }

  @GetMapping("/success")
  public String success(@RequestParam("orderId") Long orderId, Model model) {
    model.addAttribute("order", orderService.findById(orderId));
    return "customer/order-success";
  }

  private Long suggestBestVoucher(List<Voucher> vouchers, BigDecimal total, com.restaurant.model.enums.UserRank rank) {
    if (vouchers == null || vouchers.isEmpty() || total == null) {
      return null;
    }
    return vouchers.stream()
        .map(v -> new Object[] {v.getId(), voucherService.validateAndApply(v.getId(), total, rank)})
        .filter(arr -> ((VoucherResult) arr[1]).isValid())
        .max(Comparator.comparing(arr -> ((VoucherResult) arr[1]).getDiscountAmount()))
        .map(arr -> (Long) arr[0])
        .orElse(null);
  }
}

