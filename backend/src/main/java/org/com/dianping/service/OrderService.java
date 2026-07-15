package org.com.dianping.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.com.dianping.DTO.OrderResponse;
import org.com.dianping.entity.Coupon;
import org.com.dianping.entity.Merchant;
import org.com.dianping.entity.Order;
import org.com.dianping.entity.PackageGroup;
import org.com.dianping.entity.User;
import org.com.dianping.repository.CouponRepository;
import org.com.dianping.repository.MerchantRepository;
import org.com.dianping.repository.OrderRepository;
import org.com.dianping.repository.PackageGroupRepository;
import org.com.dianping.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.com.dianping.event.OrderCreated;

@Service
public class OrderService {
    private static final BigDecimal TEN = new BigDecimal("10.00");
    private final OrderRepository orderRepository;
    private final PackageGroupRepository packageRepository;
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final InvitationService invitationService;
    private final ApplicationEventPublisher events;

    public OrderService(OrderRepository orderRepository, PackageGroupRepository packageRepository,
                        CouponRepository couponRepository, CouponService couponService,
                        MerchantRepository merchantRepository, UserRepository userRepository,
                        InvitationService invitationService, ApplicationEventPublisher events) {
        this.orderRepository = orderRepository; this.packageRepository = packageRepository;
        this.couponRepository = couponRepository; this.couponService = couponService;
        this.merchantRepository = merchantRepository; this.userRepository = userRepository;
        this.invitationService = invitationService; this.events = events;
    }

    @Transactional
    public Order createOrder(Long userId, Long packageId, Long merchantId, String invitationCode, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 64) throw new IllegalArgumentException("idempotencyKey 无效");
        var existing = orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existing.isPresent()) return existing.get();
        PackageGroup pkg = packageRepository.findById(packageId).orElseThrow(() -> new IllegalArgumentException("套餐不存在"));
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new IllegalArgumentException("商家不存在"));
        if (!pkg.getMerchantId().equals(merchantId)) throw new IllegalArgumentException("套餐不属于该商家");
        BigDecimal price = money(pkg.getPrice());
        UsedCoupon used = calculateBestPrice(price, couponRepository.findValidCouponsByUserIdAndMerchant(userId, merchant.getCategory(), merchantId, price));
        if (packageRepository.decrementStockAndIncrementSales(packageId) != 1) throw new IllegalStateException("套餐库存不足");
        if (used.coupon() != null && !couponService.claimCoupon(used.coupon().getId())) throw new IllegalStateException("优惠券已使用");
        Order order = new Order();
        order.setUserId(userId); order.setPackageId(packageId); order.setCreateTime(LocalDateTime.now());
        order.setBusinessName(merchant.getMerchantName()); order.setOriginalPrice(price); order.setBestCoupon(used.coupon() == null ? null : used.coupon().getId());
        order.setFinalPrice(money(price.subtract(used.discount()).max(BigDecimal.ZERO)));
        order.setVoucherCode(UUID.randomUUID().toString().replace("-", ""));
        order.setBusinessNo("DP" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        order.setIdempotencyKey(idempotencyKey); order.setStatus("未使用");
        Order saved = orderRepository.saveAndFlush(order);
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (invitationCode != null && !invitationCode.isBlank()) bindInvitation(user, invitationCode, saved.getFinalPrice());
        else if (user.getInviterId() != null) invitationService.processInvitationReward(user.getInviterId(), userId, saved.getFinalPrice());
        events.publishEvent(OrderCreated.of(saved.getId()));
        return saved;
    }

    private void bindInvitation(User user, String code, BigDecimal amount) {
        if (user.getInviterId() != null) throw new IllegalArgumentException("已经使用过邀请码");
        User inviter = userRepository.findByInvitationCode(code).orElseThrow(() -> new IllegalArgumentException("无效的邀请码"));
        if (inviter.getId().equals(user.getId())) throw new IllegalArgumentException("不能使用自己的邀请码");
        user.setInviterId(inviter.getId()); userRepository.save(user);
        if (amount.compareTo(TEN) > 0) invitationService.processInvitationReward(inviter.getId(), user.getId(), amount);
    }

    public Order findByIdempotencyKey(Long userId, String key) { return orderRepository.findByUserIdAndIdempotencyKey(userId, key).orElseThrow(); }
    public OrderResponse getOrderDetails(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        if (!order.getUserId().equals(userId)) throw new SecurityException("无权查看该订单");
        return new OrderResponse(order, packageRepository.findById(order.getPackageId()).orElseThrow(() -> new IllegalArgumentException("套餐不存在")));
    }
    public List<OrderResponse> getUserOrders(Long userId) { return orderRepository.findByUserIdOrderByCreateTimeDesc(userId).stream().map(o -> new OrderResponse(o, packageRepository.findById(o.getPackageId()).orElseThrow())).toList(); }
    public boolean checkUserOrders(Long userId) { return orderRepository.existsByUserId(userId); }
    public UsedCoupon calculateBestPrice(BigDecimal originalPrice, List<Coupon> coupons) {
        Coupon best = null; BigDecimal discount = BigDecimal.ZERO;
        for (Coupon coupon : coupons) { BigDecimal current = computeDiscount(originalPrice, coupon); if (current.compareTo(discount) > 0) { best = coupon; discount = current; } }
        return new UsedCoupon(best, discount);
    }
    public BigDecimal computeDiscount(BigDecimal price, Coupon coupon) {
        if (coupon.getMinAmount() != null && price.compareTo(coupon.getMinAmount()) < 0) return money(BigDecimal.ZERO);
        BigDecimal discount = switch (coupon.getType()) {
            case "满减", "立减" -> coupon.getValue();
            case "折扣" -> price.multiply(BigDecimal.TEN.subtract(coupon.getValue())).divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP).min(coupon.getMaxAmount());
            case "秒杀" -> price.subtract(new BigDecimal("0.10")).max(BigDecimal.ZERO);
            case "免单" -> price;
            default -> throw new IllegalArgumentException("无效的优惠券类型");
        };
        return money(discount.min(price));
    }
    private static BigDecimal money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    public record UsedCoupon(Coupon coupon, BigDecimal discount) { }
}
