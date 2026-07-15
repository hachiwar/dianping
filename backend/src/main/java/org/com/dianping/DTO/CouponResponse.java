package org.com.dianping.DTO;
import org.com.dianping.entity.Coupon;
import java.math.BigDecimal;

public record CouponResponse(
    String couponName,
    Long userId,
    Integer couponAmount,
    String category,
    String shop_id,
    String type,
    BigDecimal value,
    BigDecimal minAmount,
    BigDecimal maxAmount,
    String expireTime) {
    public CouponResponse(Coupon coupon) {
        this(coupon.getCouponName(), coupon.getUserId(), coupon.getCouponAmount(), coupon.getCategory(), coupon.getShopId() == null ? null : coupon.getShopId().toString(), coupon.getType(), coupon.getValue(), coupon.getMinAmount(), coupon.getMaxAmount(), coupon.getExpireTime() == null ? null : coupon.getExpireTime().toString());
    }
}
