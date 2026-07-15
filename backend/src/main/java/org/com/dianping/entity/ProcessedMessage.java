package org.com.dianping.entity;
import java.time.LocalDateTime;
import jakarta.persistence.*;
@Entity @Table(name = "processed_message", uniqueConstraints = @UniqueConstraint(columnNames = "message_id"))
public class ProcessedMessage {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name = "message_id", nullable = false) private String messageId;
 @Column(nullable = false) private LocalDateTime processedAt = LocalDateTime.now();
 public ProcessedMessage() {} public ProcessedMessage(String id) { messageId = id; }
}
