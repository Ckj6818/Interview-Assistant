package com.interviewai.service;

import com.interviewai.entity.Roles;
import com.interviewai.entity.User;
import com.interviewai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private static final Set<String> RESERVED_USERNAMES = Set.of("admin", "root", "system");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(String username, String rawPassword, String confirmPassword) {
        String normalizedUsername = normalizeUsername(username);

        validateUsername(normalizedUsername);
        validatePassword(rawPassword, confirmPassword);

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("用户名已存在，请换一个试试");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Roles.USER);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        return username.trim();
    }

    private void validateUsername(String username) {
        if (username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("用户名长度需在 3～20 个字符之间");
        }
        if (!username.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")) {
            throw new IllegalArgumentException("用户名仅支持中文、字母、数字和下划线");
        }
        if (RESERVED_USERNAMES.contains(username.toLowerCase())) {
            throw new IllegalArgumentException("该用户名不可注册，请更换");
        }
    }

    private void validatePassword(String rawPassword, String confirmPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (rawPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度至少 6 位");
        }
        if (confirmPassword == null || !rawPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
    }
}
