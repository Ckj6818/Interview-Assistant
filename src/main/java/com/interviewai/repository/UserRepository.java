package com.interviewai.repository;

import com.interviewai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 根据用户名查找用户，用于登录鉴权
    Optional<User> findByUsername(String username);
    
    // 判断用户名是否存在，用于注册时校验
    boolean existsByUsername(String username);
}
