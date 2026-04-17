package com.restaurant.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  public String handleNotFound(ResourceNotFoundException ex, Model model, HttpServletRequest request) {
    model.addAttribute("path", request.getRequestURI());
    model.addAttribute("message", ex.getMessage());
    return "error/404";
  }

  @ExceptionHandler(BusinessException.class)
  public String handleBusiness(BusinessException ex, Model model, HttpServletRequest request) {
    model.addAttribute("path", request.getRequestURI());
    model.addAttribute("message", ex.getMessage());
    return "error/400";
  }

  @ExceptionHandler(Exception.class)
  public String handleGeneric(Exception ex, Model model, HttpServletRequest request) {
    model.addAttribute("path", request.getRequestURI());
    model.addAttribute("message", ex.getMessage());
    return "error/500";
  }
}

