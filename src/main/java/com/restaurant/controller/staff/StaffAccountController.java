package com.restaurant.controller.staff;

import com.restaurant.model.dto.PasswordChangeDTO;
import com.restaurant.model.dto.ProfileUpdateDTO;
import com.restaurant.model.entity.User;
import com.restaurant.exception.BusinessException;
import com.restaurant.repository.UserRepository;
import com.restaurant.security.CustomUserDetails;
import com.restaurant.service.UserService;
import com.restaurant.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/account")
@PreAuthorize("hasRole('STAFF')")
public class StaffAccountController {
  private final UserService userService;
  private final UserRepository userRepository;

  public StaffAccountController(UserService userService, UserRepository userRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
  }

  @ModelAttribute("activeMenu")
  public String activeMenu() {
    return "account";
  }

  @GetMapping
  public String page(Model model) {
    Long id = SecurityUtils.currentUserId();
    User u = userService.findById(id);
    if (!model.containsAttribute("profile")) {
      ProfileUpdateDTO p = new ProfileUpdateDTO();
      p.setFullName(u.getFullName());
      p.setPhone(u.getPhone());
      p.setEmail(u.getEmail());
      model.addAttribute("profile", p);
    }
    if (!model.containsAttribute("passwordForm")) {
      model.addAttribute("passwordForm", new PasswordChangeDTO());
    }
    return "staff/account";
  }

  @PostMapping("/profile")
  public String saveProfile(@Valid @ModelAttribute("profile") ProfileUpdateDTO profile,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.profile", bindingResult);
      redirectAttributes.addFlashAttribute("profile", profile);
      redirectAttributes.addFlashAttribute("passwordForm", new PasswordChangeDTO());
      redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin");
      return "redirect:/staff/account";
    }
    Long id = SecurityUtils.currentUserId();
    userService.updateProfile(id, profile);
    refreshAuthentication(id);
    redirectAttributes.addFlashAttribute("success", "Đã cập nhật thông tin tài khoản");
    return "redirect:/staff/account";
  }

  @PostMapping("/password")
  public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordChangeDTO form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordForm", bindingResult);
      redirectAttributes.addFlashAttribute("passwordForm", form);
      redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại mật khẩu");
      return "redirect:/staff/account";
    }
    if (!form.getNewPassword().equals(form.getConfirmPassword())) {
      redirectAttributes.addFlashAttribute("passwordForm", form);
      redirectAttributes.addFlashAttribute("error", "Mật khẩu mới nhập lại không khớp");
      return "redirect:/staff/account";
    }
    Long id = SecurityUtils.currentUserId();
    try {
      userService.changePassword(id, form.getCurrentPassword(), form.getNewPassword());
    } catch (BusinessException e) {
      redirectAttributes.addFlashAttribute("passwordForm", form);
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/staff/account";
    }
    refreshAuthentication(id);
    redirectAttributes.addFlashAttribute("success", "Đã đổi mật khẩu");
    return "redirect:/staff/account";
  }

  private void refreshAuthentication(Long userId) {
    User fresh = userRepository.findById(userId).orElseThrow();
    CustomUserDetails details = new CustomUserDetails(fresh);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(details, details.getPassword(), details.getAuthorities()));
  }
}
