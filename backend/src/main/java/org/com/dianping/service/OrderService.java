package org.com.dianping.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.com.dianping.event.OutboxService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final PackageGroupRepository packageRepository;
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher events;
    private final StringRedisTemplate redis;
    private final OutboxService outbox;
    private final RedisLockService locks;

    public OrderService(OrderRepository orderRepository, PackageGroupRepository packageRepository,
                        CouponRepository couponRepository, CouponService couponService,
                        MerchantRepository merchantRepository, UserRepository userRepository,
                        ApplicationEventPublisher events, StringRedisTemplate redis,
                        OutboxService outbox, RedisLockService locks) {
        this.orderRepository = orderRepository; this.packageRepository = packageRepository;
        this.couponRepository = couponRepository; this.couponService = couponService;
        this.merchantRepository = merchantRepository; this.userRepository = userRepository;
        this.events = events; this.redis = redis; this.outbox = outbox; this.locks = locks;
    }

    @Transactional
    public Order createOrder(Long userId, Long packageId, Long merchantId, String invitationCode, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 64) throw new IllegalArgumentException("idempotencyKey 无效");
        var existing = orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existing.isPresent()) return existing.get();
        String idempotencyRedisKey = "order:idempotency:" + userId + ":" + idempotencyKey;
        String lockKey = "lock:" + idempotencyRedisKey;
        Optional<String> lock = Optional.empty();
        boolean lockAvailable = true;
        try { lock = locks.tryAcquire(lockKey, Duration.ofSeconds(30)); }
        catch (RuntimeException ex) { lockAvailable = false; log.warn("redis lock unavailable, relying on database idempotency key={}", lockKey); }
        if (lockAvailable && lock.isEmpty()) return orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                .orElseThrow(() -> new IllegalStateException("请求处理中，请重试"));
        lock.ifPresent(owner -> TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) { locks.release(lockKey, owner); }
        }));
        PackageGroup pkg = packageRepository.findById(packageId).orElseThrow(() -> new IllegalArgumentException("套餐不存在"));
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new IllegalArgumentException("商家不存在"));
        if (!pkg.getMerchantId().equals(merchantId)) throw new IllegalArgumentException("套餐不属于该商家");
        BigDecimal price = money(pkg.getPrice());
        UsedCoupon used = calculateBestPrice(price, couponRepository.findValidCouponsByUserIdAndMerchant(userId, merchant.getCategory(), merchantId, price));
        if (packageRepository.decrementStockAndIncrementSales(packageId, pkg.getVersion()) != 1)
            throw new IllegalStateException("套餐已更新或库存不足，请重试");
        if (used.coupon() != null && !couponService.claimCoupon(used.coupon().getId())) throw new IllegalStateException("优惠券已使用");
        Order order = new Order();
        order.setUserId(userId); order.setPackageId(packageId); order.setCreateTime(LocalDateTime.now());
        order.setBusinessName(merchant.getMerchantName()); order.setOriginalPrice(price); order.setBestCoupon(used.coupon() == null ? null : used.coupon().getId());
        order.setFinalPrice(money(price.subtract(used.discount()).max(BigDecimal.ZERO)));
        order.setBusinessNo("DP" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        order.setIdempotencyKey(idempotencyKey); order.setStatus("未使用");
        Order saved = orderRepository.saveAndFlush(order);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                try { redis.opsForValue().set(idempotencyRedisKey, saved.getId().toString(), Duration.ofHours(24)); }
                catch (RuntimeException ex) { log.warn("order idempotency result cache failed key={}", idempotencyRedisKey); }
            }
        });
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setOrderCount(user.getOrderCount() + 1);
        userRepository.save(user);
        if (invitationCode != null && !invitationCode.isBlank()) bindInvitation(user, invitationCode);
        OrderCreated event = OrderCreated.of(saved.getId());
        outbox.record(event);
        events.publishEvent(event);
        return saved;
    }

    private void bindInvitation(User user, String code) {
        if (user.getInviterId() != null) throw new IllegalArgumentException("已经使用过邀请码");
        User inviter = userRepository.findByInvitationCode(code).orElseThrow(() -> new IllegalArgumentException("无效的邀请码"));
        if (inviter.getId().equals(user.getId())) throw new IllegalArgumentException("不能使用自己的邀请码");
        user.setInviterId(inviter.getId()); userRepository.save(user);
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
