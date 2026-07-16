package org.com.dianping.DTO;
import org.com.dianping.entity.Review;

public record ReviewResponse(
    Long id,
    Long userID,
    Long merchantID,
    Double rating,
    String comment,
    Long parentID,
    String createTime) {
    public ReviewResponse(Review review) {
        this(review.getId(), review.getUserID(), review.getMerchantID(), review.getRating(), review.getComment(), review.getParentID(), review.getCreateTime().toString());
    }
}
