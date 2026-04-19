package com.restaurant.repository;

import com.restaurant.model.entity.Voucher;
import com.restaurant.model.enums.UserRank;
import com.restaurant.model.enums.VoucherType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
  boolean existsByCodeAndIsDeletedFalse(String code);

  List<Voucher> findByIsDeletedFalse();

  @Query("""
      select v
      from Voucher v
      where v.status = true and v.isDeleted = false
        and :now between v.startDate and v.endDate
        and (v.quantity = -1 or v.usedCount < v.quantity)
      order by v.discountPercent desc
      """)
  List<Voucher> findAvailable(@Param("now") LocalDateTime now);

  @Query("""
      select v
      from Voucher v
      where v.status = true and v.isDeleted = false
        and :now between v.startDate and v.endDate
        and (v.quantity = -1 or v.usedCount < v.quantity)
        and v.type = :type
      order by v.discountPercent desc
      """)
  List<Voucher> findAvailableByType(@Param("type") VoucherType type, @Param("now") LocalDateTime now);

  @Query("""
      select v
      from Voucher v
      where v.status = true and v.isDeleted = false
        and :now between v.startDate and v.endDate
        and (v.quantity = -1 or v.usedCount < v.quantity)
        and v.type = com.restaurant.model.enums.VoucherType.ONLINE_RANK_ONLY
        and (v.minRank is null or v.minRank in :allowedRanks)
      order by v.discountPercent desc
      """)
  List<Voucher> findOnlineAvailableForRanks(@Param("allowedRanks") List<UserRank> allowedRanks,
      @Param("now") LocalDateTime now);
}
