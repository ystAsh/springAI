/*
 * =============================================================================
 * 클래스명 : SecurityConfig
 * =============================================================================
 * 목적
 *  - URL별 인증 및 접근 권한을 설정한다.
 *  - 브라우저 로그인과 HTTP Basic 인증을 함께 지원한다.
 *  - 로그인 성공 시 기존 대화를 삭제하고 Spring AI 대화 화면으로 이동한다.
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

    // 비밀번호 암호화 객체를 등록한다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    // HTTP 요청별 인증 규칙을 설정한다.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationSuccessHandler
                    customAuthenticationSuccessHandler
    ) throws Exception {

        http
                // 현재 REST API 테스트를 위해 CSRF를 비활성화한다.
                .csrf(csrf ->
                        csrf.disable()
                )

                // URL별 접근 권한을 설정한다.
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(
                                        "/",
                                        "/error",
                                        "/login",
                                        "/css/**",
                                        "/js/**",
                                        "/images/**",
                                        "/h2-console/**"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/chat",
                                        "/chat/**",
                                        "/api/chat/**",
                                        "/me",
                                        "/permission/**",
                                        "/conversations/**",
                                        "/api/vector-documents/**"
                                )
                                .authenticated()

                                .anyRequest()
                                .permitAll()
                )

                // H2 콘솔의 frame 사용을 허용한다.
                .headers(headers ->
                        headers.frameOptions(
                                frameOptions ->
                                        frameOptions.sameOrigin()
                        )
                )

                // 로그인 성공 시 기존 대화를 삭제하는 성공 핸들러를 실행한다.
                .formLogin(form ->
                        form
                                .successHandler(
                                        customAuthenticationSuccessHandler
                                )
                                .permitAll()
                )

                // test.http의 Basic 인증을 허용한다.
                .httpBasic(
                        Customizer.withDefaults()
                )

                // 로그아웃 성공 후 로그인 화면으로 이동한다.
                .logout(logout ->
                        logout
                                .logoutSuccessUrl(
                                        "/login?logout"
                                )
                                .permitAll()
                );

        return http.build();
    }
}