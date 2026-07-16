package org.com.dianping.repository;

import org.com.dianping.entity.InvitationReward;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvitationRewardRepository extends JpaRepository<InvitationReward, Long> {
    List<InvitationReward> findByUserId(Long userId);
}