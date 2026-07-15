package org.com.dianping.controller;

import java.util.List;
import org.com.dianping.entity.Coupon;
import org.com.dianping.service.CouponService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    private final CouponService coupons;
    public CouponController(CouponService coupons) { this.coupons = coupons; }
    @GetMapping("/all") public List<Coupon> all(@RequestHeader("UserId") Long userId) { return coupons.getAllCouponsByUserId(userId); }
    @PostMapping("/issue-by-choice") public void issueNewUserCoupon(@RequestHeader("UserId") Long userId, @RequestParam char choice) { coupons.issueNewUserCoupons(userId, choice); }
    @PostMapping("/use/{couponId}") public void use(@RequestHeader("UserId") Long userId, @PathVariable Long couponId) { coupons.useCoupon(userId, couponId); }
}
