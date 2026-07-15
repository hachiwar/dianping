package org.com.dianping.repository;

import java.util.List;
import java.util.Optional;

import org.com.dianping.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    // 根据ID和用户ID查询优惠券
    Optional<Coupon> findByIdAndUserId(Long couponId, Long userId);
    
    // 查询用户所有有效优惠券(amount > 0)
    @Query("SELECT c FROM Coupon c WHERE c.userId = :userId AND c.couponAmount > 0")
    List<Coupon> findValidCouponsByUserId(@Param("userId") Long userId);
    
    // 查询用户所有优惠券的基本信息
    
    // 新增：查询用户有效优惠券(按商家种类)
    @Query("SELECT c FROM Coupon c WHERE c.userId = :userId AND c.couponAmount > 0 " +
           "AND (c.expireTime IS NULL OR c.expireTime > CURRENT_TIMESTAMP) " +
           "AND (c.category IS NULL OR c.category = :merchantCategory) " +
           "AND (c.shopId IS NULL OR c.shopId = :merchantId) " +
           "AND (c.minAmount <= :initPrice OR c.minAmount IS NULL)")
    List<Coupon> findValidCouponsByUserIdAndMerchant(
        @Param("userId") Long userId,
        @Param("merchantCategory") String merchantCategory,
        @Param("merchantId") Long merchantId,
        @Param("initPrice") java.math.BigDecimal initPrice);

    @Modifying
    @Query("UPDATE Coupon c SET c.couponAmount = c.couponAmount - 1 WHERE c.id = :couponId AND c.couponAmount > 0")
    int claim(@Param("couponId") Long couponId);

    // 根据用户 ID 查询优惠券
    List<Coupon> findByUserId(Long userId);
}
