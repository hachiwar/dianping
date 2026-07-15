package org.com.dianping.repository;

import org.com.dianping.entity.InvitationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvitationRecordRepository extends JpaRepository<InvitationRecord, Long> {
    long countByUserId(Long userId);
    List<InvitationRecord> findByUserId(Long userId);
    boolean existsByUserIdAndInviteeId(Long userId, Long inviteeId);
}
