package com.restaurant.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;

public class MenuItemDTO {
  @NotBlank
  @Size(min = 2, max = 200)
  private String name;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true)
  @Digits(integer = 17, fraction = 2)
  private BigDecimal price;

  @Size(max = 1000)
  private String description;

  @NotNull
  private Long categoryId;

  private MultipartFile imageFile;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  public MultipartFile getImageFile() {
    return imageFile;
  }

  public void setImageFile(MultipartFile imageFile) {
    this.imageFile = imageFile;
  }
}

