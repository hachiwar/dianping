package org.com.dianping.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.List;
import org.com.dianping.entity.Coupon;
import org.junit.jupiter.api.Test;

class OrderServiceTest {
    private final OrderService service = new OrderService(null, null, null, null, null, null, null);

    @Test
    void calculatesCouponDiscountsWithTwoDecimalRounding() {
        Coupon discount = coupon("折扣", "8", "20");
        Coupon reduction = coupon("满减", "38", null);
        reduction.setMinAmount(new BigDecimal("100"));
        assertEquals(new BigDecimal("6.00"), service.computeDiscount(new BigDecimal("30.00"), discount));
        assertEquals(new BigDecimal("0.00"), service.computeDiscount(new BigDecimal("99.99"), reduction));
        assertEquals(new BigDecimal("38.00"), service.calculateBestPrice(new BigDecimal("100.00"), List.of(discount, reduction)).discount());
    }

    @Test
    void neverDiscountsBelowZero() {
        Coupon free = coupon("免单", "0", null);
        assertEquals(new BigDecimal("0.10"), service.computeDiscount(new BigDecimal("0.10"), free));
    }

    private Coupon coupon(String type, String value, String max) {
        Coupon coupon = new Coupon();
        coupon.setType(type); coupon.setValue(new BigDecimal(value)); coupon.setMaxAmount(max == null ? null : new BigDecimal(max));
        return coupon;
    }
}
