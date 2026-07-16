package org.com.dianping.DTO;

public record NewUserRequest(
        String username,
        String password,
        String captcha) {
}