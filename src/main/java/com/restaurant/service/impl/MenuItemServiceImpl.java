package com.restaurant.service.impl;

import com.restaurant.exception.BusinessException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.dto.MenuItemDTO;
import com.restaurant.model.entity.Category;
import com.restaurant.model.entity.MenuItem;
import com.restaurant.model.enums.MenuItemStatus;
import com.restaurant.repository.CategoryRepository;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.service.MenuItemService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class MenuItemServiceImpl implements MenuItemService {
  private final MenuItemRepository menuItemRepository;
  private final CategoryRepository categoryRepository;

  private final Path uploadDir;
  private final String webPathPrefix;

  public MenuItemServiceImpl(MenuItemRepository menuItemRepository,
      CategoryRepository categoryRepository,
      @Value("${app.upload.dir}") String uploadDir,
      @Value("${app.upload.web-path-prefix}") String webPathPrefix) {
    this.menuItemRepository = menuItemRepository;
    this.categoryRepository = categoryRepository;
    this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    this.webPathPrefix = webPathPrefix;
  }

  @Override
  @Transactional(readOnly = true)
  public List<MenuItem> getAllAvailable() {
    return menuItemRepository.findByStatus(MenuItemStatus.AVAILABLE);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MenuItem> getAll() {
    return menuItemRepository.findByStatusNot(MenuItemStatus.DELETED);
  }

  @Override
  @Transactional(readOnly = true)
  public MenuItem getById(Long id) {
    return menuItemRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy menu item id=" + id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<MenuItem> getByCategory(Long categoryId) {
    return menuItemRepository.findByStatusAndCategoryId(MenuItemStatus.AVAILABLE, categoryId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MenuItem> searchByName(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return getAllAvailable();
    }
    return menuItemRepository.findByStatusAndNameContainingIgnoreCase(MenuItemStatus.AVAILABLE, keyword.trim());
  }

  @Override
  public MenuItem create(MenuItemDTO dto) {
    Category category = categoryRepository.findById(dto.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy category id=" + dto.getCategoryId()));

    MenuItem item = new MenuItem();
    item.setName(dto.getName().trim());
    item.setPrice(dto.getPrice());
    item.setDescription(dto.getDescription());
    item.setCategory(category);

    if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
      item.setImageUrl(storeImage(dto.getImageFile()));
    }
    item.setStatus(MenuItemStatus.AVAILABLE);
    return menuItemRepository.save(item);
  }

  @Override
  public MenuItem toggleStatus(Long id) {
    MenuItem item = getById(id);
    if (item.getStatus() == MenuItemStatus.AVAILABLE) {
      item.setStatus(MenuItemStatus.OUT_OF_STOCK);
    } else {
      item.setStatus(MenuItemStatus.AVAILABLE);
    }
    return menuItemRepository.save(item);
  }

  @Override
  public void delete(Long id) {
    MenuItem item = getById(id);
    item.setStatus(MenuItemStatus.DELETED);
    menuItemRepository.save(item);
  }

  private String storeImage(MultipartFile file) {
    try {
      Files.createDirectories(uploadDir);
      String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
      String ext = "";
      int idx = original.lastIndexOf('.');
      if (idx >= 0 && idx < original.length() - 1) {
        ext = original.substring(idx).toLowerCase();
      }
      String filename = UUID.randomUUID() + ext;
      Path target = uploadDir.resolve(filename);
      file.transferTo(target);
      return normalizeWebPath(webPathPrefix) + filename;
    } catch (IOException ex) {
      throw new BusinessException("Upload ảnh thất bại: " + ex.getMessage());
    }
  }

  private String normalizeWebPath(String prefix) {
    if (prefix == null || prefix.isBlank()) {
      return "/uploads/";
    }
    String p = prefix.trim();
    if (!p.startsWith("/")) {
      p = "/" + p;
    }
    if (!p.endsWith("/")) {
      p = p + "/";
    }
    return p;
  }
}
