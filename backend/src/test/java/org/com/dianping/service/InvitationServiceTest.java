package org.com.dianping.service;

import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import org.com.dianping.repository.*;
import org.junit.jupiter.api.Test;

class InvitationServiceTest {
    @Test void duplicateInviteeDoesNotCreateAnotherReward() {
        InvitationRecordRepository records = mock(InvitationRecordRepository.class); when(records.existsByUserIdAndInviteeId(1L, 2L)).thenReturn(true);
        InvitationService service = new InvitationService(records, mock(InvitationRewardRepository.class), mock(CouponService.class), mock(UserRepository.class));
        service.processInvitationReward(1L, 2L, new BigDecimal("20.00"));
        verify(records, never()).save(any());
    }
}
