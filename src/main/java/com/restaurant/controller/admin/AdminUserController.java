package com.restaurant.controller.admin;

import com.restaurant.model.entity.User;
import com.restaurant.model.enums.UserRole;
import com.restaurant.repository.UserRepository;
import com.restaurant.service.UserService;
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
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
  private final UserService userService;
  private final UserRepository userRepository;

  public AdminUserController(UserService userService, UserRepository userRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
  }

  @org.springframework.web.bind.annotation.ModelAttribute("activeMenu")
  public String activeMenu() {
    return "users";
  }

  @GetMapping
  public String list(@RequestParam(value = "role", required = false) UserRole role,
                     @RequestParam(value = "q", required = false) String q,
                     Model model) {
    List<User> users = (role == null) ? userRepository.findAll() : userService.findByRole(role);
    if (q != null && !q.isBlank()) {
      String kw = q.trim().toLowerCase();
      users = users.stream()
          .filter(u -> u.getUsername().toLowerCase().contains(kw) || u.getFullName().toLowerCase().contains(kw))
          .toList();
    }
    model.addAttribute("users", users);
    model.addAttribute("role", role);
    model.addAttribute("q", q);
    return "admin/manage-users";
  }

  @PostMapping("/{id}/ban")
  public String ban(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    userService.banUser(id);
    redirectAttributes.addFlashAttribute("success", "Đã khóa tài khoản");
    return "redirect:/admin/users";
  }

  @PostMapping("/{id}/unban")
  public String unban(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    userService.unbanUser(id);
    redirectAttributes.addFlashAttribute("success", "Đã mở khóa tài khoản");
    return "redirect:/admin/users";
  }
}
