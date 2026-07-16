package org.com.dianping.controller;

import org.com.dianping.DTO.*;
import org.com.dianping.entity.Coupon;
import org.com.dianping.entity.InvitationReward;
import org.com.dianping.entity.User;
import org.com.dianping.repository.*;
import org.com.dianping.security.CurrentUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class InvitationController {
    private final InvitationRecordRepository recordRepo;
    private final InvitationRewardRepository rewardRepo;
    private final CouponRepository couponRepo;
    private final UserRepository userRepo;

    public InvitationController(InvitationRecordRepository recordRepo,
                                InvitationRewardRepository rewardRepo,
                                CouponRepository couponRepo,
                                UserRepository userRepo) {
        this.recordRepo = recordRepo;
        this.rewardRepo = rewardRepo;
        this.couponRepo = couponRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/invitation-records")
    public List<InvitationRecordResponse> getRecords() {
        Long userId = CurrentUser.id();
        return recordRepo.findByUserId(userId).stream()
                .map(r -> new InvitationRecordResponse(
                        r.getId(),
                        userRepo.findById(r.getInviteeId()).map(User::getUsername).orElse("未知用户"),
                        r.getOrderTime(),
                        r.getPrice()
                )).collect(Collectors.toList());
    }

    @GetMapping("/reward-coupons")
    public List<InvitationRewardResponse> getRewards() {
        Long userId = CurrentUser.id();
        return rewardRepo.findByUserId(userId).stream()
                .map(r -> {
                    Coupon c = couponRepo.findById(r.getCouponId()).orElse(null);
                    return new InvitationRewardResponse(
                            r.getId(),
                            c != null ? c.getId() : null,
                            c != null ? c.getCouponName() : "已过期优惠券",
                            r.getTotalCount(), // 使用实际保存的总邀请人数
                            c != null ? c.getExpireTime().minusDays(7) : null,
                            c != null ? c.getExpireTime() : null
                    );
                }).collect(Collectors.toList());
    }
}
