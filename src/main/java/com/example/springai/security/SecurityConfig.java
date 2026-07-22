/*
 * =============================================================================
 * 클래스명 : SecurityConfig
 * =============================================================================
 * 목적
 *  - URL별 인증 및 접근 권한을 설정하는 클래스
 */

package com.example.springai.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /* 비밀번호 암호화 객체를 등록한다. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /* HTTP 요청별 인증 규칙을 설정한다. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                /* REST API 테스트를 위해 CSRF 비활성화 */
                .csrf(csrf -> csrf.disable())

                /* URL별 접근 권한 설정 */
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/",
                                "/error",
                                "/login",
                                "/h2-console/**"
                        ).permitAll()

                        /* 로그인한 사용자만 접근 가능 */
                        .requestMatchers(
                                "/chat/**",
                                "/me",
                                "/permission/**",
                                "/conversations/**"
                        ).authenticated()

                        .anyRequest()
                        .permitAll()
                )

                /* H2 콘솔의 frame 사용 허용 */
                .headers(headers -> headers
                        .frameOptions(frameOptions ->
                                frameOptions.sameOrigin()
                        )
                )

                /* HTTP Basic 인증 사용 */
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}