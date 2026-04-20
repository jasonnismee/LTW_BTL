package com.restaurant.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  public String handleNotFound(ResourceNotFoundException ex, Model model, HttpServletRequest request) {
    model.addAttribute("path", request.getRequestURI());
    model.addAttribute("message", ex.getMessage());
    return "error/404";
  }

  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  public String handleNoResource(org.springframework.web.servlet.resource.NoResourceFoundException ex, Model model,
      HttpServletRequest request) {
    model.addAttribute("path", request.getRequestURI());
    model.addAttribute("message", "Không tìm thấy tài nguyên");
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
    ex.printStackTrace();
    try {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      ex.printStackTrace(pw);
      Files.writeString(Paths.get("d:/LTW_BTL/last_error.log"), sw.toString());
    } catch (Exception ignored) {
    }
    model.addAttribute("path", request.getRequestURI());
    model.addAttribute("message", ex.getMessage());
    return "error/500";
  }
}
