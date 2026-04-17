package com.restaurant.controller.admin;

import com.restaurant.model.dto.VoucherDTO;
import com.restaurant.model.enums.VoucherType;
import com.restaurant.service.VoucherService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/vouchers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVoucherController {
  private final VoucherService voucherService;

  public AdminVoucherController(VoucherService voucherService) {
    this.voucherService = voucherService;
  }

  @org.springframework.web.bind.annotation.ModelAttribute("activeMenu")
  public String activeMenu() {
    return "vouchers";
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("vouchers", voucherService.getAll());
    return "admin/manage-vouchers";
  }

  @GetMapping("/create")
  public String createForm(Model model) {
    if (!model.containsAttribute("voucher")) {
      VoucherDTO dto = new VoucherDTO();
      dto.setType(VoucherType.ONLINE_RANK_ONLY);
      model.addAttribute("voucher", dto);
    }
    return "admin/voucher-form";
  }

  @PostMapping
  public String create(@Valid VoucherDTO voucher, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.voucher", bindingResult);
      redirectAttributes.addFlashAttribute("voucher", voucher);
      redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin voucher");
      return "redirect:/admin/vouchers/create";
    }
    voucherService.create(voucher);
    redirectAttributes.addFlashAttribute("success", "Đã tạo voucher");
    return "redirect:/admin/vouchers";
  }

  @PutMapping("/{id}")
  public String update(@PathVariable("id") Long id, @Valid VoucherDTO voucher, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin voucher");
      return "redirect:/admin/vouchers";
    }
    voucherService.update(id, voucher);
    redirectAttributes.addFlashAttribute("success", "Đã cập nhật voucher");
    return "redirect:/admin/vouchers";
  }

  @PostMapping("/{id}/toggle-status")
  public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    voucherService.toggleStatus(id);
    redirectAttributes.addFlashAttribute("success", "Đã đổi trạng thái voucher");
    return "redirect:/admin/vouchers";
  }

  @DeleteMapping("/{id}")
  public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    voucherService.delete(id);
    redirectAttributes.addFlashAttribute("success", "Đã xóa voucher");
    return "redirect:/admin/vouchers";
  }
}

