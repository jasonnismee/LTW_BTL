package com.restaurant.controller.customer;

import com.restaurant.service.CartService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/customer/cart")
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {
  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping
  public String view(Model model) {
    model.addAttribute("cartItems", cartService.getCart());
    model.addAttribute("total", cartService.getCartTotal());
    return "customer/cart";
  }

  @PostMapping("/add")
  public String add(@RequestParam("menuItemId") @NotNull Long menuItemId,
                    @RequestParam("quantity") @NotNull @Min(1) Integer quantity,
                    @RequestParam(value = "returnUrl", required = false) String returnUrl,
                    RedirectAttributes redirectAttributes) {
    cartService.addToCart(menuItemId, quantity);
    redirectAttributes.addFlashAttribute("success", "Đã thêm món vào giỏ hàng");
    if (isSafeCustomerReturnUrl(returnUrl)) {
      return "redirect:" + returnUrl;
    }
    return "redirect:/customer/cart";
  }

  /** Chỉ cho phép redirect nội bộ /customer/... để tránh open redirect. */
  private static boolean isSafeCustomerReturnUrl(String url) {
    if (url == null || url.isBlank()) {
      return false;
    }
    String u = url.trim();
    if (!u.startsWith("/customer/") || u.contains("..") || u.contains("//") || u.contains("\r") || u.contains("\n")) {
      return false;
    }
    return true;
  }

  @PostMapping("/proceed-to-checkout")
  public String proceedToCheckout(@RequestParam("menuItemId") List<Long> menuItemIds,
                                    @RequestParam("quantity") List<Integer> quantities,
                                    RedirectAttributes redirectAttributes) {
    if (menuItemIds == null || quantities == null || menuItemIds.size() != quantities.size()) {
      redirectAttributes.addFlashAttribute("error", "Dữ liệu giỏ hàng không hợp lệ");
      return "redirect:/customer/cart";
    }
    for (int i = 0; i < menuItemIds.size(); i++) {
      cartService.updateQuantity(menuItemIds.get(i), quantities.get(i));
    }
    return "redirect:/customer/checkout";
  }

  @PostMapping("/remove")
  public String remove(@RequestParam("menuItemId") @NotNull Long menuItemId,
                       RedirectAttributes redirectAttributes) {
    cartService.removeItem(menuItemId);
    redirectAttributes.addFlashAttribute("success", "Đã xóa món khỏi giỏ hàng");
    return "redirect:/customer/cart";
  }

  @PostMapping("/clear")
  public String clear(RedirectAttributes redirectAttributes) {
    cartService.clearCart();
    redirectAttributes.addFlashAttribute("success", "Đã xóa toàn bộ giỏ hàng");
    return "redirect:/customer/cart";
  }
}

