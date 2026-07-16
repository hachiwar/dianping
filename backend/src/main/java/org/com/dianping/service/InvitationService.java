package org.com.dianping.service;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import org.com.dianping.entity.Coupon;
import org.com.dianping.entity.InvitationRecord;
import org.com.dianping.entity.InvitationReward;
import org.com.dianping.entity.User;
import org.com.dianping.repository.InvitationRecordRepository;
import org.com.dianping.repository.InvitationRewardRepository;
import org.com.dianping.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvitationService {
    private final InvitationRecordRepository recordRepo;
    private final InvitationRewardRepository rewardRepo;
    private final CouponService couponService;
    private final UserRepository userRepo;

    public InvitationService(InvitationRecordRepository recordRepo,
                             InvitationRewardRepository rewardRepo,
                             CouponService couponService,
                             UserRepository userRepo) {
        this.recordRepo = recordRepo;
        this.rewardRepo = rewardRepo;
        this.couponService = couponService;
        this.userRepo = userRepo;
    }

    @Transactional
    public void processInvitationReward(Long inviterId, Long inviteeId, BigDecimal orderAmount) {
        if (recordRepo.existsByUserIdAndInviteeId(inviterId, inviteeId)) return;
        // 检查订单金额是否满足要求
        if (orderAmount.compareTo(new BigDecimal("10.00")) <= 0) {
            return; // 订单金额不满足要求，不记录邀请
        }

        // 获取被邀请人信息
        User invitee = userRepo.findById(inviteeId)
                .orElseThrow(() -> new RuntimeException("被邀请用户不存在"));

        // 保存邀请记录
        InvitationRecord record = new InvitationRecord();
        record.setUserId(inviterId);
        record.setInviteeId(inviteeId);
        record.setInviteeName(invitee.getUsername());
        record.setOrderTime(LocalDateTime.now());
        record.setPrice(orderAmount);
        recordRepo.save(record);

        // 检查奖励条件
        long totalValid = recordRepo.countByUserId(inviterId);
        if (totalValid % 2 == 0) {
            createRewardCoupon(inviterId, inviteeId);
        }
    }

    private void createRewardCoupon(Long inviterId, Long inviteeId) {
        Coupon coupon = new Coupon();
        coupon.setUserId(inviterId);
        coupon.setCouponName("20元无门槛优惠券");
        coupon.setType("立减");
        coupon.setValue(new BigDecimal("20.00"));
        coupon.setMinAmount(BigDecimal.ZERO);
        coupon.setCouponAmount(1);
        coupon.setExpireTime(LocalDateTime.now().plusDays(7));
        couponService.saveCoupon(coupon);

        InvitationReward reward = new InvitationReward();
        reward.setUserId(inviterId);
        reward.setInviteeId(inviteeId);
        reward.setCouponId(coupon.getId());
        reward.setTotalCount(recordRepo.countByUserId(inviterId));
        rewardRepo.save(reward);
    }
}
