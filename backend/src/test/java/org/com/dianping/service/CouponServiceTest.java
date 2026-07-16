package org.com.dianping.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.com.dianping.repository.CouponRepository;
import org.com.dianping.repository.UserRepository;
import org.junit.jupiter.api.Test;

class CouponServiceTest {
    @Test void atomicallyClaimsCouponForItsOwner() {
        CouponRepository coupons = mock(CouponRepository.class); when(coupons.claimForUser(8L, 2L)).thenReturn(1);
        assertDoesNotThrow(() -> new CouponService(coupons, mock(UserRepository.class)).useCoupon(2L, 8L));
        verify(coupons).claimForUser(8L, 2L);
    }
    @Test void rejectsDuplicateOrUnauthorizedClaim() {
        CouponRepository coupons = mock(CouponRepository.class); when(coupons.claimForUser(8L, 2L)).thenReturn(0);
        assertThrows(IllegalStateException.class, () -> new CouponService(coupons, mock(UserRepository.class)).useCoupon(2L, 8L));
    }
}
