package com.restaurant.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.restaurant.model.dto.VoucherResult;
import com.restaurant.model.entity.Voucher;
import com.restaurant.model.enums.UserRank;
import com.restaurant.model.enums.VoucherType;
import com.restaurant.repository.VoucherRepository;
import com.restaurant.service.impl.VoucherServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {
  @Mock
  private VoucherRepository voucherRepository;

  private VoucherServiceImpl voucherService;

  @BeforeEach
  void setUp() {
    voucherService = new VoucherServiceImpl(voucherRepository);
  }

  @Test
  void testValidate_VoucherValid_ReturnsCorrectDiscount() {
    Voucher v = baseVoucher();
    v.setDiscountPercent(10);
    when(voucherRepository.findById(1L)).thenReturn(Optional.of(v));

    VoucherResult result = voucherService.validateAndApply(1L, new BigDecimal("200000"), UserRank.BRONZE);
    assertTrue(result.isValid());
    assertEquals(new BigDecimal("20000.00"), result.getDiscountAmount());
    assertEquals(new BigDecimal("180000.00"), result.getFinalPrice());
  }

  @Test
  void testValidate_VoucherExpired_ReturnsFalse() {
    Voucher v = baseVoucher();
    v.setStartDate(LocalDateTime.now().minusDays(10));
    v.setEndDate(LocalDateTime.now().minusDays(1));
    when(voucherRepository.findById(1L)).thenReturn(Optional.of(v));

    VoucherResult result = voucherService.validateAndApply(1L, new BigDecimal("200000"), UserRank.BRONZE);
    assertFalse(result.isValid());
  }

  @Test
  void testValidate_VoucherRankTooHigh_ReturnsFalse() {
    Voucher v = baseVoucher();
    v.setMinRank(UserRank.GOLD);
    when(voucherRepository.findById(1L)).thenReturn(Optional.of(v));

    VoucherResult result = voucherService.validateAndApply(1L, new BigDecimal("200000"), UserRank.SILVER);
    assertFalse(result.isValid());
  }

  @Test
  void testValidate_MaxDiscountCapped_ReturnsMaxAmount() {
    Voucher v = baseVoucher();
    v.setDiscountPercent(50);
    v.setMaxDiscountAmount(new BigDecimal("30000"));
    when(voucherRepository.findById(1L)).thenReturn(Optional.of(v));

    VoucherResult result = voucherService.validateAndApply(1L, new BigDecimal("200000"), UserRank.BRONZE);
    assertTrue(result.isValid());
    assertEquals(new BigDecimal("30000"), result.getDiscountAmount());
    assertEquals(0, result.getFinalPrice().compareTo(new BigDecimal("170000.00")));
  }

  private Voucher baseVoucher() {
    Voucher v = new Voucher();
    v.setId(1L);
    v.setCode("WELCOME10");
    v.setType(VoucherType.ONLINE_RANK_ONLY);
    v.setMinRank(UserRank.BRONZE);
    v.setMinOrderAmount(new BigDecimal("100000"));
    v.setQuantity(-1);
    v.setUsedCount(0);
    v.setStatus(true);
    v.setStartDate(LocalDateTime.now().minusDays(1));
    v.setEndDate(LocalDateTime.now().plusDays(1));
    v.setDiscountPercent(10);
    return v;
  }
}

