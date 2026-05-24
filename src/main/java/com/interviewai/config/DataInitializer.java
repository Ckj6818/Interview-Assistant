package com.interviewai.config;

import com.interviewai.entity.User;
import com.interviewai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User defaultUser = new User();
            defaultUser.setUsername("user");
            defaultUser.setPassword(passwordEncoder.encode("123456"));
            defaultUser.setRole("ROLE_USER");
            userRepository.save(defaultUser);
            System.out.println("=============== 初始化默认测试用户完成 (账号: user, 密码: 123456) ===============");
        }
    }
}
