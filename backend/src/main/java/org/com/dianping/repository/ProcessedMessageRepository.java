package org.com.dianping.repository;
import org.com.dianping.entity.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, Long> { boolean existsByMessageId(String messageId); }
