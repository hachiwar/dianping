package org.com.dianping.service;

import org.com.dianping.DTO.LoginRequest;
import org.com.dianping.DTO.NewUserRequest;
import org.com.dianping.DTO.UserResponse;
import org.com.dianping.Utils.PasswordUtil;
import org.com.dianping.entity.User;
import org.com.dianping.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 * <p>
 * 提供用户相关的业务逻辑实现，包括用户注册、登录等功能
 * </p>
 *
 * @author Software Engineering Group
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    /**
     * 构造函数
     *
     * @param userRepository 用户数据访问接口
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 检查用户名是否已存在
     *
     * @param username 待检查的用户名
     * @return true表示用户名已存在，false表示用户名可用
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 注册新用户
     * <p>
     * 此方法会进行如下验证：
     * <ul>
     *   <li>用户名格式 (4-20位字母、数字或下划线)</li>
     *   <li>密码格式 (至少6位，必须包含字母和数字)</li>
     *   <li>用户名是否已存在</li>
     * </ul>
     * </p>
     *
     * @param request 包含用户注册信息的请求对象
     * @return 注册成功的用户信息
     * @throws RuntimeException 当用户名格式不合法、密码格式不合法或用户名已存在时抛出
     */
    public UserResponse registerUser(NewUserRequest request) {
        if (!request.username().matches("^[A-Za-z0-9_]{4,20}$")) {
            throw new RuntimeException("用户名格式不合法");
        }
        if (!request.password().matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")) {
            throw new RuntimeException("密码格式不合法");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("用户名已存在");
        }
        String encryptedPassword = PasswordUtil.encryptPassword(request.password());
        var user = new User(null, request.username(), encryptedPassword);

        // 只生成邀请码，不处理邀请关系
        String invitationCode;
        do {
            invitationCode = generateInvitationCode();
        } while (userRepository.existsByInvitationCode(invitationCode));

        user.setInvitationCode(invitationCode);
        user = userRepository.save(user);
        return new UserResponse(user);
    }

    private String generateInvitationCode() {
        // Generate 6 characters code with numbers and uppercase letters
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (chars.length() * Math.random());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 用户登录
     * <p>
     * 验证用户名和密码，成功则返回用户信息
     * </p>
     *
     * @param request 包含登录信息的请求对象
     * @return 登录用户的信息
     * @throws RuntimeException 当用户不存在或密码错误时抛出
     */
    public UserResponse loginUser(LoginRequest request) {
        var userOpt = userRepository.findByUsername(request.username());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        User user = userOpt.get();
        if (!PasswordUtil.checkPassword(request.password(), user.getEncryptedPassword())) {
            throw new RuntimeException("密码错误");
        }
        return new UserResponse(user);
    }

    /**
     * 验证邀请码
     * @param invitationCode 邀请码
     * @return 邀请人用户对象
     * @throws RuntimeException 当邀请码无效时抛出
     */
    public User validateInvitationCode(String invitationCode) {
        if (invitationCode == null || invitationCode.isEmpty()) {
            throw new RuntimeException("邀请码不能为空");
        }
        return userRepository.findByInvitationCode(invitationCode)
            .orElseThrow(() -> new RuntimeException("无效的邀请码"));
    }

    /**
     * 验证邀请码并返回邀请人的用户名
     * @param invitationCode 邀请码
     * @return 包含验证结果和邀请人用户名的对象
     */
    public InvitationValidationResponse validateInvitationCodeForOrder(String invitationCode) {
        try {
            User inviter = validateInvitationCode(invitationCode);
            return new InvitationValidationResponse(true, inviter.getUsername(), null);
        } catch (RuntimeException e) {
            return new InvitationValidationResponse(false, null, e.getMessage());
        }
    }

    // 添加一个新的记录类来封装验证结果
    public record InvitationValidationResponse(
        boolean valid,
        String inviterUsername,
        String message
    ) {}
}
