package com.example.springai.config;

import com.example.springai.entity.AppUser;
import com.example.springai.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class TestDataInitializer {

    @Bean
    public CommandLineRunner initializeUsers(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (appUserRepository.findByUsername("admin").isEmpty()) {

                AppUser admin = new AppUser(
                        "admin",
                        passwordEncoder.encode("admin1234"),
                        "ADMIN",
                        "COMPANY01",
                        "IT",
                        5
                );

                appUserRepository.save(admin);
            }

            if (appUserRepository.findByUsername("user1").isEmpty()) {

                AppUser user = new AppUser(
                        "user1",
                        passwordEncoder.encode("user1234"),
                        "USER",
                        "COMPANY01",
                        "SALES",
                        2
                );

                appUserRepository.save(user);
            }
        };
    }
}