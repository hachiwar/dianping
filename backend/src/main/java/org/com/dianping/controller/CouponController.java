package org.com.dianping.controller;

import java.util.List;
import org.com.dianping.entity.Coupon;
import org.com.dianping.service.CouponService;
import org.com.dianping.security.CurrentUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    private final CouponService coupons;
    public CouponController(CouponService coupons) { this.coupons = coupons; }
    @GetMapping("/all") public List<Coupon> all() { return coupons.getAllCouponsByUserId(CurrentUser.id()); }
    @PostMapping("/issue-by-choice") public void issueNewUserCoupon(@RequestParam char choice) { coupons.issueNewUserCoupons(CurrentUser.id(), choice); }
    @PostMapping("/use/{couponId}") public void use(@PathVariable Long couponId) { coupons.useCoupon(CurrentUser.id(), couponId); }
}
