package org.com.dianping.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "coupon")
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String couponName;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private Integer couponAmount;
    private String category;
    private Long shopId;
    @Column(nullable = false) private String type;
    @Column(precision = 19, scale = 2) private BigDecimal value;
    @Column(precision = 19, scale = 2) private BigDecimal maxAmount;
    @Column(precision = 19, scale = 2) private BigDecimal minAmount;
    private LocalDateTime expireTime;
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getCouponName() { return couponName; } public void setCouponName(String v) { couponName = v; }
    public Long getUserId() { return userId; } public void setUserId(Long v) { userId = v; }
    public Integer getCouponAmount() { return couponAmount; } public void setCouponAmount(Integer v) { couponAmount = v; }
    public String getCategory() { return category; } public void setCategory(String v) { category = v; }
    public Long getShopId() { return shopId; } public void setShopId(Long v) { shopId = v; }
    public String getType() { return type; } public void setType(String v) { type = v; }
    public BigDecimal getValue() { return value; } public void setValue(BigDecimal v) { value = v; }
    public BigDecimal getMinAmount() { return minAmount; } public void setMinAmount(BigDecimal v) { minAmount = v; }
    public void setMiniAmount(BigDecimal v) { minAmount = v; }
    public BigDecimal getMaxAmount() { return maxAmount; } public void setMaxAmount(BigDecimal v) { maxAmount = v; }
    public LocalDateTime getExpireTime() { return expireTime; } public void setExpireTime(LocalDateTime v) { expireTime = v; }
}
