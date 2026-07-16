package org.com.dianping.observability;

import org.com.dianping.event.DeadLetterService;
import org.com.dianping.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OperationalAlertLogger {
    private static final Logger log = LoggerFactory.getLogger(OperationalAlertLogger.class);
    private final OutboxEventRepository outboxEvents;
    private final DeadLetterService deadLetters;
    public OperationalAlertLogger(OutboxEventRepository outboxEvents, DeadLetterService deadLetters) { this.outboxEvents = outboxEvents; this.deadLetters = deadLetters; }
    @Scheduled(fixedDelayString = "${alerts.check-interval-ms:60000}")
    public void reportMessageBacklog() {
        long outbox = outboxEvents.count(); int deadLetter = deadLetters.pending();
        if (outbox > 0 || deadLetter > 0) log.error("operational-alert outboxPending={} deadLetterPending={}", outbox, deadLetter);
    }
}
