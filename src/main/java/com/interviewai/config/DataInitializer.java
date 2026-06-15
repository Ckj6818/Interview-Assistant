package com.interviewai.config;

import com.interviewai.entity.Roles;
import com.interviewai.entity.User;
import com.interviewai.repository.QuestionRepository;
import com.interviewai.repository.UserRepository;
import com.interviewai.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        seedQuestionsIfEmpty();
        int removed = questionService.deduplicateQuestions();
        if (removed > 0) {
            System.out.println("=============== 已清理重复题目 " + removed + " 条 ===============");
        }

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

    private void seedQuestionsIfEmpty() throws Exception {
        if (questionRepository.count() > 0) {
            return;
        }
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("data.sql"));
        populator.setContinueOnError(true);
        populator.execute(dataSource);
        System.out.println("=============== 首次启动：已导入初始题库 data.sql ===============");
    }
}
