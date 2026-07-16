package org.com.dianping.DTO;

import java.time.LocalDateTime;

public record InvitationRecordResponse(
        Long id,
        String inviteeName,
        LocalDateTime orderTime,
        java.math.BigDecimal price
) {}
