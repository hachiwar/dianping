package org.com.dianping.controller;

import java.util.Collections;
import java.util.Map;

import org.com.dianping.DTO.NewUserRequest;
import org.com.dianping.DTO.UserResponse;
import org.com.dianping.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for managing user-related operations such as registration and username checks.
 */
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructs a new {@link UserController}.
     *
     * @param userService the user service
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Checks if a username is already taken.
     *
     * @param username the username to check
     * @return a map indicating whether the username exists
     */
    @GetMapping("/check-username")
    public Map<String, Boolean> checkUsernameAvailability(@RequestParam String username) {
        boolean exists = userService.usernameExists(username);
        return Collections.singletonMap("exists", exists);
    }

    /**
     * Registers a new user after validating the input.
     *
     * @param request the new user request containing username, password, and captcha
     * @param session the HTTP session to retrieve the captcha code
     * @return the registered user's response
     * @throws RuntimeException if validation fails
     */
    @PostMapping
    public UserResponse registerUser(@RequestBody NewUserRequest request, HttpSession session) {
        String sessionCaptcha = (String) session.getAttribute("CAPTCHA_CODE");
        if (sessionCaptcha == null || !sessionCaptcha.equals(request.captcha())) {
            throw new RuntimeException("验证码错误");
        }
        if (!request.username().matches("^[A-Za-z0-9_]{4,20}$")) {
            throw new RuntimeException("用户名格式不合法");
        }
        if (!request.password().matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")) {
            throw new RuntimeException("密码格式不合法");
        }
        session.removeAttribute("CAPTCHA_CODE");
        return userService.registerUser(request);
    }

 
    /**
     * 处理订单的邀请码验证请求
     * @param invitationCode 邀请码
     * @return 验证结果
     */
    @GetMapping("/validate-invitation")
    public ResponseEntity<?> validateInvitationCodeForOrder(@RequestParam String invitationCode) {
        try {
            var response = userService.validateInvitationCodeForOrder(invitationCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", e.getMessage()
            ));
        }
    }
}