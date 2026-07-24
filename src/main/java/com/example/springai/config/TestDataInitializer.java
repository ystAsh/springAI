/*
 * =============================================================================
 * 클래스명 : TestDataInitializer
 * =============================================================================
 * 목적
 *  - 애플리케이션 실행 시 테스트용 사용자 데이터를 생성한다.
 */

package com.example.springai.config;

import com.example.springai.entity.AppUser;
import com.example.springai.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestDataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataInitializer(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (appUserRepository.findByUsername("admin").isEmpty()) {

            AppUser admin = new AppUser();

            admin.setUsername("admin");

            // 비밀번호를 암호화하여 저장한다.
            admin.setPassword(
                    passwordEncoder.encode("admin1234")
            );

            admin.setRole("ADMIN");
            admin.setOrganizationId("ORG001");
            admin.setDepartmentId("ADMIN");
            admin.setSecurityLevel(5);

            appUserRepository.save(admin);
        }
    }
}