package org.com.dianping.repository;

import java.util.List;

import org.com.dianping.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMerchantID(Long merchantID);
    List<Review> findByUserID(Long userID);
    List<Review> findByParentID(Long parentID);
}
