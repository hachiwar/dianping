package org.com.dianping.repository;

import java.util.Optional;

import org.com.dianping.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 判断用户名是否存在
    boolean existsByUsername(String username);

    // 根据用户名查找用户
    Optional<User> findByUsername(String username);

    // 添加检查邀请码是否存在的方法
    boolean existsByInvitationCode(String invitationCode);

    Optional<User> findByInvitationCode(String invitationCode);
}
