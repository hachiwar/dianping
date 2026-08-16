package org.com.dianping.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.com.dianping.DTO.NewUserRequest;
import org.com.dianping.DTO.UserResponse;
import org.com.dianping.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class UserControllerTest {
    @Test
    void registrationCreatesTheAuthenticatedSession() {
        UserService users = mock(UserService.class);
        NewUserRequest request = new NewUserRequest("demo_user", "demo123", "ABCD");
        UserResponse response = new UserResponse(1L, "demo_user", 0, "ABC123");
        when(users.registerUser(request)).thenReturn(response);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("CAPTCHA_CODE", "ABCD");

        assertEquals(response, new UserController(users).registerUser(request, session));
        assertEquals(response, session.getAttribute("USER_SESSION"));
    }
}
