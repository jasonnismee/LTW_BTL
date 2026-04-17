package com.restaurant.service.impl;

import com.restaurant.exception.BusinessException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.dto.VoucherDTO;
import com.restaurant.model.dto.VoucherResult;
import com.restaurant.model.entity.Voucher;
import com.restaurant.model.enums.UserRank;
import com.restaurant.model.enums.VoucherType;
import com.restaurant.repository.VoucherRepository;
import com.restaurant.service.VoucherService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VoucherServiceImpl implements VoucherService {
  private final VoucherRepository voucherRepository;

  public VoucherServiceImpl(VoucherRepository voucherRepository) {
    this.voucherRepository = voucherRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Voucher> getAll() {
    return voucherRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Voucher findById(Long id) {
    return voucherRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher id=" + id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Voucher> getAvailableVouchers() {
    return voucherRepository.findAvailable(LocalDateTime.now());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Voucher> getOnlineVouchersForRank(UserRank rank) {
    List<UserRank> allowed = allowedRanks(rank);
    return voucherRepository.findOnlineAvailableForRanks(allowed, LocalDateTime.now());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Voucher> getOfflineVouchers() {
    return voucherRepository.findAvailableByType(VoucherType.OFFLINE_ALL, LocalDateTime.now());
  }

  @Override
  public Voucher create(VoucherDTO dto) {
    Voucher voucher = new Voucher();
    String normalizedCode = dto.getCode() == null ? null : dto.getCode().trim().toUpperCase();
    if (normalizedCode == null || normalizedCode.isBlank()) {
      throw new BusinessException("Voucher code không hợp lệ");
    }
    if (voucherRepository.existsByCode(normalizedCode)) {
      throw new BusinessException("Voucher code đã tồn tại");
    }

    voucher.setCode(normalizedCode);
    voucher.setDiscountPercent(dto.getDiscountPercent());
    voucher.setMaxDiscountAmount(dto.getMaxDiscountAmount());
    voucher.setMinOrderAmount(dto.getMinOrderAmount() == null ? BigDecimal.ZERO : dto.getMinOrderAmount());
    voucher.setType(dto.getType());
    voucher.setMinRank(dto.getMinRank());
    voucher.setQuantity(dto.getQuantity());
    voucher.setUsedCount(0);
    voucher.setStartDate(dto.getStartDate());
    voucher.setEndDate(dto.getEndDate());
    voucher.setStatus(dto.getStatus());

    if (voucher.getType() == VoucherType.ONLINE_RANK_ONLY && voucher.getMinRank() == null) {
      throw new BusinessException("Voucher ONLINE_RANK_ONLY phải có minRank");
    }
    return voucherRepository.save(voucher);
  }

  @Override
  public Voucher update(Long id, VoucherDTO dto) {
    Voucher voucher = findById(id);
    voucher.setDiscountPercent(dto.getDiscountPercent());
    voucher.setMaxDiscountAmount(dto.getMaxDiscountAmount());
    voucher.setMinOrderAmount(dto.getMinOrderAmount() == null ? BigDecimal.ZERO : dto.getMinOrderAmount());
    voucher.setType(dto.getType());
    voucher.setMinRank(dto.getMinRank());
    voucher.setQuantity(dto.getQuantity());
    voucher.setStartDate(dto.getStartDate());
    voucher.setEndDate(dto.getEndDate());
    voucher.setStatus(dto.getStatus());

    if (voucher.getType() == VoucherType.ONLINE_RANK_ONLY && voucher.getMinRank() == null) {
      throw new BusinessException("Voucher ONLINE_RANK_ONLY phải có minRank");
    }
    return voucherRepository.save(voucher);
  }

  @Override
  public Voucher toggleStatus(Long id) {
    Voucher voucher = findById(id);
    voucher.setStatus(!Boolean.TRUE.equals(voucher.getStatus()));
    return voucherRepository.save(voucher);
  }

  @Override
  @Transactional(readOnly = true)
  public VoucherResult validateAndApply(Long voucherId, BigDecimal totalPrice, UserRank rank) {
    // Quy tắc voucher:
    // 1) Phải tồn tại + đang active
    // 2) Thời gian hiện tại nằm trong startDate..endDate
    // 3) Còn lượt dùng: quantity = -1 (unlimited) hoặc usedCount < quantity
    // 4) Tổng tiền >= minOrderAmount
    // 5) ONLINE_RANK_ONLY: rank hiện tại phải >= minRank
    // 6) Discount = totalPrice * percent, có maxDiscountAmount thì cap (min)
    if (voucherId == null) {
      return new VoucherResult(false, BigDecimal.ZERO, totalPrice, "Không có voucher");
    }
    if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) < 0) {
      return new VoucherResult(false, BigDecimal.ZERO, BigDecimal.ZERO, "Tổng tiền không hợp lệ");
    }

    Voucher voucher = voucherRepository.findById(voucherId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher id=" + voucherId));

    LocalDateTime now = LocalDateTime.now();
    if (!Boolean.TRUE.equals(voucher.getStatus())) {
      return new VoucherResult(false, BigDecimal.ZERO, totalPrice, "Voucher không hợp lệ");
    }
    if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
      return new VoucherResult(false, BigDecimal.ZERO, totalPrice, "Voucher đã hết hạn");
    }
    if (!(voucher.getQuantity() != null && (voucher.getQuantity() == -1 || voucher.getUsedCount() < voucher.getQuantity()))) {
      return new VoucherResult(false, BigDecimal.ZERO, totalPrice, "Voucher đã hết lượt sử dụng");
    }
    BigDecimal minOrder = voucher.getMinOrderAmount() == null ? BigDecimal.ZERO : voucher.getMinOrderAmount();
    if (totalPrice.compareTo(minOrder) < 0) {
      return new VoucherResult(false, BigDecimal.ZERO, totalPrice, "Đơn hàng chưa đủ điều kiện áp dụng voucher");
    }
    if (voucher.getType() == VoucherType.ONLINE_RANK_ONLY) {
      if (rank == null) {
        return new VoucherResult(false, BigDecimal.ZERO, totalPrice, "Voucher chỉ áp dụng cho đơn ONLINE");
      }
      if (voucher.getMinRank() != null && rank.ordinal() < voucher.getMinRank().ordinal()) {
        return new VoucherResult(false, BigDecimal.ZERO, totalPrice, "Rank không đủ điều kiện áp dụng voucher");
      }
    }

    BigDecimal percent = BigDecimal.valueOf(voucher.getDiscountPercent()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    BigDecimal discount = totalPrice.multiply(percent).setScale(2, RoundingMode.HALF_UP);
    if (voucher.getMaxDiscountAmount() != null) {
      discount = discount.min(voucher.getMaxDiscountAmount());
    }
    if (discount.compareTo(BigDecimal.ZERO) < 0) {
      discount = BigDecimal.ZERO;
    }
    BigDecimal finalPrice = totalPrice.subtract(discount);
    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
      finalPrice = BigDecimal.ZERO;
    }
    return new VoucherResult(true, discount, finalPrice, "Áp dụng thành công");
  }

  @Override
  public void incrementUsedCount(Long voucherId) {
    Voucher voucher = voucherRepository.findById(voucherId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher id=" + voucherId));
    voucher.setUsedCount(voucher.getUsedCount() + 1);
    voucherRepository.save(voucher);
  }

  @Override
  public void delete(Long id) {
    if (!voucherRepository.existsById(id)) {
      throw new ResourceNotFoundException("Không tìm thấy voucher id=" + id);
    }
    voucherRepository.deleteById(id);
  }

  private List<UserRank> allowedRanks(UserRank rank) {
    UserRank safeRank = rank == null ? UserRank.BRONZE : rank;
    List<UserRank> allowed = new ArrayList<>();
    for (UserRank r : UserRank.values()) {
      if (r.ordinal() <= safeRank.ordinal()) {
        allowed.add(r);
      }
    }
    return allowed;
  }
}

