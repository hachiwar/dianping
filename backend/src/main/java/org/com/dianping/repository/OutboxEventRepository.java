package org.com.dianping.repository;

import java.util.List;
import org.com.dianping.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop100ByOrderByIdAsc();
    @Transactional void deleteByMessageId(String messageId);
}
