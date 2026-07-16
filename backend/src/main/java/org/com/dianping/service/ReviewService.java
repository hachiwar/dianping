package org.com.dianping.service;

import java.time.LocalDateTime;
import java.util.List;
import org.com.dianping.entity.Review;
import org.com.dianping.repository.MerchantRepository;
import org.com.dianping.repository.ReviewRepository;
import org.com.dianping.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final ReviewRepository reviews;
    private final MerchantRepository merchants;
    private final UserRepository users;
    private final CouponService coupons;
    public ReviewService(ReviewRepository reviews, MerchantRepository merchants, UserRepository users, CouponService coupons) { this.reviews = reviews; this.merchants = merchants; this.users = users; this.coupons = coupons; }
    public List<Review> getReviewsByMerchantID(Long merchantId) { return reviews.findByMerchantID(merchantId); }
    public List<Review> getReviewsByUserID(Long userId) { return reviews.findByUserID(userId); }
    public List<Review> getReviewsByParentID(Long parentId) { return reviews.findByParentID(parentId); }
    @Transactional
    public void createReview(Review review) {
        if (!merchants.existsById(review.getMerchantID())) throw new IllegalArgumentException("商家不存在");
        if (!users.existsById(review.getUserID())) throw new IllegalArgumentException("用户不存在");
        if (review.getComment() == null || review.getComment().isBlank()) throw new IllegalArgumentException("评论不能为空");
        int usefulCount = (int) reviews.findByUserID(review.getUserID()).stream().filter(value -> value.getComment().length() >= 15).count();
        review.setCreateTime(LocalDateTime.now()); reviews.save(review);
        if (usefulCount == 2 && review.getComment().length() >= 15) coupons.issueReviewRewardCoupon(review.getUserID());
    }
}
