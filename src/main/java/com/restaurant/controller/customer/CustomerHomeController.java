package com.restaurant.controller.customer;

import com.restaurant.model.entity.MenuItem;
import com.restaurant.service.CategoryService;
import com.restaurant.service.MenuItemService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerHomeController {
  private final MenuItemService menuItemService;
  private final CategoryService categoryService;

  public CustomerHomeController(MenuItemService menuItemService, CategoryService categoryService) {
    this.menuItemService = menuItemService;
    this.categoryService = categoryService;
  }

  @GetMapping({"", "/", "/home"})
  public String home(@RequestParam(value = "q", required = false) String q,
                     @RequestParam(value = "categoryId", required = false) Long categoryId,
                     Model model) {
    List<MenuItem> items;
    if (categoryId != null) {
      items = menuItemService.getByCategory(categoryId);
      if (q != null && !q.isBlank()) {
        String kw = q.trim().toLowerCase();
        items = items.stream().filter(i -> i.getName().toLowerCase().contains(kw)).toList();
      }
    } else {
      items = (q == null || q.isBlank()) ? menuItemService.getAllAvailable() : menuItemService.searchByName(q);
    }

    model.addAttribute("items", items);
    model.addAttribute("categories", categoryService.getAllCategories());
    model.addAttribute("q", q);
    model.addAttribute("categoryId", categoryId);
    return "customer/home";
  }

  @GetMapping("/menu/{id}")
  public String menuDetail(@PathVariable("id") Long id, Model model) {
    model.addAttribute("item", menuItemService.getById(id));
    return "customer/menu-detail";
  }

  @GetMapping("/category/{id}/items")
  public String byCategory(@PathVariable("id") Long categoryId,
                           @RequestParam(value = "q", required = false) String q,
                           Model model) {
    return home(q, categoryId, model);
  }
}

