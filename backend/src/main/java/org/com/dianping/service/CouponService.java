package org.com.dianping.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.com.dianping.entity.Coupon;
import org.com.dianping.entity.User;
import org.com.dianping.repository.CouponRepository;
import org.com.dianping.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {
    private final CouponRepository coupons;
    private final UserRepository users;
    public CouponService(CouponRepository coupons, UserRepository users) { this.coupons = coupons; this.users = users; }
    public List<Coupon> getValidCouponsForUser(Long userId, String category) { return coupons.findValidCouponsByUserId(userId).stream().filter(c -> c.getCategory() == null || c.getCategory().equals(category)).toList(); }
    public List<Coupon> getAllCouponsByUserId(Long userId) { return coupons.findByUserId(userId); }
    @Transactional
    public void issueNewUserCoupons(Long userId, char choice) {
        User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (user.getOrderCount() != 0) return;
        if (coupons.existsByUserIdAndSource(userId, "NEW_USER")) throw new IllegalStateException("新人优惠券已领取");
        Coupon coupon = new Coupon();
        coupon.setUserId(userId); coupon.setCouponAmount(1); coupon.setShopId(null); coupon.setSource("NEW_USER"); coupon.setMaxAmount(new BigDecimal("9999999999999999.00"));
        switch (choice) {
            case 'A' -> { coupon.setType("满减"); coupon.setCategory("火锅"); coupon.setMinAmount(new BigDecimal("100.00")); coupon.setValue(new BigDecimal("38.00")); coupon.setCouponName("满100减38元(火锅专用券)"); coupon.setExpireTime(LocalDateTime.now().plusDays(7)); }
            case 'B' -> { coupon.setType("折扣"); coupon.setCategory("奶茶"); coupon.setMinAmount(new BigDecimal("9.00")); coupon.setValue(new BigDecimal("8.00")); coupon.setCouponName("满9元8折券（奶茶专用券）"); coupon.setExpireTime(LocalDateTime.now().plusDays(7)); }
            case 'C' -> { coupon.setType("秒杀"); coupon.setCategory("奶茶"); coupon.setMinAmount(BigDecimal.ZERO); coupon.setValue(BigDecimal.ZERO); coupon.setCouponName("奶茶畅喝秒杀券"); coupon.setExpireTime(LocalDateTime.now().plusDays(1)); }
            case 'D' -> { coupon.setType("立减"); coupon.setMinAmount(BigDecimal.ZERO); coupon.setValue(new BigDecimal("10.00")); coupon.setCouponName("通用立减10元券"); }
            case 'E' -> { coupon.setType("折扣"); coupon.setMinAmount(BigDecimal.ZERO); coupon.setValue(new BigDecimal("8.00")); coupon.setMaxAmount(new BigDecimal("20.00")); coupon.setCouponName("通用8折券,最高抵扣20元"); coupon.setExpireTime(LocalDateTime.now().plusDays(7)); }
            default -> throw new IllegalArgumentException("无效的优惠券选择");
        }
        coupons.save(coupon);
    }
    public void saveCoupon(Coupon coupon) { coupons.save(coupon); }
    public void issueReviewRewardCoupon(Long userId) {
        Coupon coupon = new Coupon();
        coupon.setUserId(userId); coupon.setCouponAmount(1); coupon.setType("折扣"); coupon.setMinAmount(BigDecimal.ZERO); coupon.setValue(new BigDecimal("8.00")); coupon.setMaxAmount(new BigDecimal("20.00")); coupon.setCouponName("优质评论奖励券"); coupon.setExpireTime(LocalDateTime.now().plusDays(7));
        coupons.save(coupon);
    }
    @Transactional
    public void useCoupon(Long userId, Long couponId) { if (coupons.claimForUser(couponId, userId) != 1) throw new IllegalStateException("优惠券不存在、无权使用或已使用"); }
    public boolean claimCoupon(Long couponId) { return coupons.claim(couponId) == 1; }
}
