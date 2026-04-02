package com.user_service;

import com.user_service.model.Role;
import com.user_service.model.User;
import com.user_service.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    /**
     * Password encoder bean used by AuthService
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Initializes a default admin user.
     * Runs ONLY in dev/prod — NOT during tests.
     */
    @Bean
    @Profile("!test")
    CommandLineRunner initAdmin(UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {

        return args -> {

            if (userRepository.findByEmail("admin@userservice.com").isEmpty()) {

                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@userservice.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ROLE_ADMIN);

                // optional fields

                userRepository.save(admin);

                System.out.println("=================================");
                System.out.println("Admin user created successfully");
                System.out.println("Email: admin@userservice.com");
                System.out.println("Password: admin123");
                System.out.println("=================================");
            } else {
                System.out.println("Admin user already exists.");
            }
        };
    }
}