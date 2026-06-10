package com.interviewai.config;

import com.interviewai.entity.Roles;
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
        if (!userRepository.existsByUsername("user")) {
            User defaultUser = new User();
            defaultUser.setUsername("user");
            defaultUser.setPassword(passwordEncoder.encode("123456"));
            defaultUser.setRole(Roles.USER);
            userRepository.save(defaultUser);
            System.out.println("=============== 初始化普通用户完成 (账号: user, 密码: 123456) ===============");
        }

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(Roles.ADMIN);
            userRepository.save(admin);
            System.out.println("=============== 初始化管理员账号完成 (账号: admin, 密码: 123456) ===============");
        } else {
            User admin = userRepository.findByUsername("admin").orElseThrow();
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(Roles.ADMIN);
            userRepository.save(admin);
        }
    }
}
