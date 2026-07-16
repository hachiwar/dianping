package org.com.dianping.Utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * <p>
 * 提供密码加密和验证功能，使用BCrypt加密算法
 * </p>
 *
 * @author Software Engineering Group
 */
public class PasswordUtil {
    /**
     * BCrypt密码编码器实例
     */
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     * <p>
     * 使用BCrypt算法对密码进行加密
     * </p>
     *
     * @param password 原始密码
     * @return 加密后的密码字符串
     */
    public static final String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 校验密码
     * <p>
     * 验证输入的密码是否与加密后的密码匹配
     * </p>
     *
     * @param password 待验证的原始密码
     * @param encryptPassword 已加密的密码
     * @return true表示密码匹配，false表示密码不匹配
     */
    public static boolean checkPassword(String password, String encryptPassword) {
        return passwordEncoder.matches(password, encryptPassword);
    }
}
