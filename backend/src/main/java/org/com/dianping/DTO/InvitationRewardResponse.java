package org.com.dianping.DTO;

import java.time.LocalDateTime;

public record InvitationRewardResponse(
        Long id,
        Long couponId,
        String couponName,
        Long count,
        LocalDateTime atTime,
        LocalDateTime expireTime
) {
}