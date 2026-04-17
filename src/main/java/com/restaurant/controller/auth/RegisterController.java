package com.restaurant.controller.auth;

import com.restaurant.model.dto.UserDTO;
import com.restaurant.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class RegisterController {
  private final UserService userService;

  public RegisterController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/register")
  public String registerForm(Model model) {
    if (!model.containsAttribute("user")) {
      model.addAttribute("user", new UserDTO());
    }
    return "auth/register";
  }

  @PostMapping("/register")
  public String register(@Valid UserDTO user, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
      redirectAttributes.addFlashAttribute("user", user);
      redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin");
      return "redirect:/auth/register";
    }
    userService.createUser(user);
    redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Vui lòng đăng nhập.");
    return "redirect:/auth/login";
  }
}

