package com.restaurant.controller.admin;

import com.restaurant.model.dto.MenuItemDTO;
import com.restaurant.service.CategoryService;
import com.restaurant.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/menu")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMenuController {
  private final CategoryService categoryService;
  private final MenuItemService menuItemService;

  public AdminMenuController(CategoryService categoryService, MenuItemService menuItemService) {
    this.categoryService = categoryService;
    this.menuItemService = menuItemService;
  }

  @org.springframework.web.bind.annotation.ModelAttribute("activeMenu")
  public String activeMenu() {
    return "menu";
  }

  @GetMapping
  public String page(Model model) {
    model.addAttribute("categories", categoryService.getAllCategories());
    model.addAttribute("items", menuItemService.getAll());
    if (!model.containsAttribute("menuItem")) {
      model.addAttribute("menuItem", new MenuItemDTO());
    }
    return "admin/manage-menu";
  }

  @PostMapping("/category")
  public String createCategory(@RequestParam("name") String name, RedirectAttributes redirectAttributes) {
    categoryService.createCategory(name);
    redirectAttributes.addFlashAttribute("success", "Đã tạo danh mục");
    return "redirect:/admin/menu";
  }

  @PutMapping("/category/{id}")
  public String updateCategory(@PathVariable("id") Long id,
      @RequestParam("name") String name,
      RedirectAttributes redirectAttributes) {
    categoryService.updateCategory(id, name);
    redirectAttributes.addFlashAttribute("success", "Đã cập nhật danh mục");
    return "redirect:/admin/menu";
  }

  @DeleteMapping("/category/{id}")
  public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    categoryService.deleteCategory(id);
    redirectAttributes.addFlashAttribute("success", "Đã xóa danh mục");
    return "redirect:/admin/menu";
  }

  @PostMapping("/item")
  public String createItem(@Valid MenuItemDTO menuItem, BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.menuItem", bindingResult);
      redirectAttributes.addFlashAttribute("menuItem", menuItem);
      redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin món ăn");
      return "redirect:/admin/menu";
    }
    menuItemService.create(menuItem);
    redirectAttributes.addFlashAttribute("success", "Đã tạo món ăn");
    return "redirect:/admin/menu";
  }

  @PostMapping("/item/{id}/toggle-status")
  public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    menuItemService.toggleStatus(id);
    redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái món ăn");
    return "redirect:/admin/menu";
  }

  @DeleteMapping("/item/{id}")
  public String deleteItem(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    menuItemService.delete(id);
    redirectAttributes.addFlashAttribute("success", "Đã xóa món ăn");
    return "redirect:/admin/menu";
  }
}
