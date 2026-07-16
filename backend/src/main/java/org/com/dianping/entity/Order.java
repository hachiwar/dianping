package org.com.dianping.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "orders", uniqueConstraints = {
        @UniqueConstraint(name = "uk_order_business_no", columnNames = "business_no"),
        @UniqueConstraint(name = "uk_order_user_idempotency", columnNames = {"user_id", "idempotency_key"}),
        @UniqueConstraint(name = "uk_order_voucher_code", columnNames = "voucher_code") })
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "package_id", nullable = false) private Long packageId;
    @Column(nullable = false) private LocalDateTime createTime;
    @Column(nullable = false) private String businessName;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal originalPrice;
    @Column(name = "best_coupon_id") private Long bestCouponId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal finalPrice;
    @Column(name = "voucher_code", nullable = false, length = 32) private String voucherCode;
    @Column(name = "business_no", nullable = false, length = 32) private String businessNo;
    @Column(name = "idempotency_key", nullable = false, length = 64) private String idempotencyKey;
    @Column(nullable = false) private String status;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getPackageId() { return packageId; }
    public String getBusinessName() { return businessName; }
    public String getVoucherCode() { return voucherCode; }
    public String getBusinessNo() { return businessNo; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setUserId(Long value) { userId = value; }
    public void setPackageId(Long value) { packageId = value; }
    public void setCreateTime(LocalDateTime value) { createTime = value; }
    public void setBusinessName(String value) { businessName = value; }
    public void setOriginalPrice(BigDecimal value) { originalPrice = value; }
    public void setBestCoupon(Long value) { bestCouponId = value; }
    public void setFinalPrice(BigDecimal value) { finalPrice = value; }
    public void setVoucherCode(String value) { voucherCode = value; }
    public void setBusinessNo(String value) { businessNo = value; }
    public void setIdempotencyKey(String value) { idempotencyKey = value; }
    public void setStatus(String value) { status = value; }
}
