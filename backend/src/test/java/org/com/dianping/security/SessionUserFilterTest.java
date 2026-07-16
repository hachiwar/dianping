package org.com.dianping.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.com.dianping.DTO.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;

class SessionUserFilterTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void authenticatesUsingServerSideSessionInsteadOfClientHeader() throws Exception {
        MockHttpSession session = new MockHttpSession(); session.setAttribute("USER_SESSION", new UserResponse(9L, "tester", 0, "INV001"));
        MockHttpServletRequest request = new MockHttpServletRequest(); request.setSession(session);
        new SessionUserFilter().doFilter(request, new MockHttpServletResponse(), (req, res) -> assertEquals(9L, CurrentUser.id()));
    }
}
