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
                    RedirectAttributes redirectAttributes) {
    cartService.addToCart(menuItemId, quantity);
    redirectAttributes.addFlashAttribute("success", "Đã thêm món vào giỏ hàng");
    return "redirect:/customer/cart";
  }

  @PostMapping("/update")
  public String update(@RequestParam("menuItemId") @NotNull Long menuItemId,
                       @RequestParam("quantity") @NotNull @Min(1) Integer quantity,
                       RedirectAttributes redirectAttributes) {
    cartService.updateQuantity(menuItemId, quantity);
    redirectAttributes.addFlashAttribute("success", "Đã cập nhật số lượng");
    return "redirect:/customer/cart";
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

