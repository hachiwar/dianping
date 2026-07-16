package org.com.dianping.entity;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
@Entity
@Table(name = "invitation_record", uniqueConstraints = @UniqueConstraint(name = "uk_invitation_invitee", columnNames = {"user_id", "invitee_id"}))
//查看自己的邀请记录：包括成功邀请的新用户列表、邀请成功订单的下单时间与实付金额。
public class InvitationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long inviteeId;

    @Column(nullable = false)
    private LocalDateTime orderTime;//同order的createTime

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String inviteeName;  // 添加被邀请人名称字段

    public InvitationRecord() {
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
    public LocalDateTime getOrderTime() {
        return orderTime;
    }
    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public String getInviteeName() {
        return inviteeName;
    }

    public void setInviteeName(String inviteeName) {
        this.inviteeName = inviteeName;
    }
}
