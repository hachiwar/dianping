package org.com.dianping.security;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentUserTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void readsAuthenticatedSessionPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(7L, null, AuthorityUtils.createAuthorityList("ROLE_USER")));
        assertEquals(7L, CurrentUser.id());
    }
    @Test void rejectsMissingAuthentication() { assertThrows(AccessDeniedException.class, CurrentUser::id); }
}
