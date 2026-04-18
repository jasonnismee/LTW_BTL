package com.restaurant.controller.admin;

import com.restaurant.model.dto.UserDTO;
import com.restaurant.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/staff")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStaffController {
  private final UserService userService;

  public AdminStaffController(UserService userService) {
    this.userService = userService;
  }

  @ModelAttribute("activeMenu")
  public String activeMenu() {
    return "staffCreate";
  }

  @GetMapping("/create")
  public String form(Model model) {
    if (!model.containsAttribute("staff")) {
      model.addAttribute("staff", new UserDTO());
    }
    return "admin/staff-create";
  }

  @PostMapping("/create")
  public String create(@Valid @ModelAttribute("staff") UserDTO staff,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.staff", bindingResult);
      redirectAttributes.addFlashAttribute("staff", staff);
      redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin");
      return "redirect:/admin/staff/create";
    }
    userService.createStaffUser(staff);
    redirectAttributes.addFlashAttribute("success", "Đã tạo tài khoản nhân viên");
    return "redirect:/admin/staff/create";
  }
}
