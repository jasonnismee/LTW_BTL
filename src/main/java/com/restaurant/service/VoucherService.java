package com.restaurant.service;

import com.restaurant.model.dto.VoucherDTO;
import com.restaurant.model.dto.VoucherResult;
import com.restaurant.model.entity.Voucher;
import com.restaurant.model.enums.UserRank;
import java.math.BigDecimal;
import java.util.List;

public interface VoucherService {
  List<Voucher> getAll();

  Voucher findById(Long id);

  List<Voucher> getAvailableVouchers();

  List<Voucher> getOnlineVouchersForRank(UserRank rank);

  List<Voucher> getOfflineVouchers();

  Voucher create(VoucherDTO dto);

  Voucher update(Long id, VoucherDTO dto);

  Voucher toggleStatus(Long id);

  VoucherResult validateAndApply(Long voucherId, BigDecimal totalPrice, UserRank rank);

  void incrementUsedCount(Long voucherId);

  void delete(Long id);
}

