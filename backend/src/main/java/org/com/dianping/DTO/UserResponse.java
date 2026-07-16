package org.com.dianping.DTO;

import org.com.dianping.entity.User;
import java.io.Serializable;

public record UserResponse(
        Long id,
        String username,
        Integer orderCount,
        String invitationCode) implements Serializable {
    public UserResponse(User user) {
        this(user.getId(), user.getUsername(), user.getOrderCount(), user.getInvitationCode());
    }
}
