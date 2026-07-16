package org.com.dianping.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
@Entity
@Table(name = "invitation_reward")
//查看已获得的奖励券明细：包括发放时间与优惠券内容。
public class InvitationReward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long inviteeId;

    @Column(nullable = false)
    private Long couponId;

    @Column(nullable = false)
    private Long totalCount;//总的邀请人数

    public InvitationReward() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getInviteeId() {
        return inviteeId;
    }

    public void setInviteeId(Long inviteeId) {
        this.inviteeId = inviteeId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
}
