/*
 * =============================================================================
 * 클래스명 : SecurityConfig
 * =============================================================================
 * 목적
 *  - URL별 인증 및 접근 권한을 설정한다.
 *  - 브라우저 로그인과 HTTP Basic 인증을 함께 지원한다.
 *  - 브라우저 세션 요청은 CSRF로 보호한다.
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationSuccessHandler
                    customAuthenticationSuccessHandler
    ) throws Exception {

        http
                /*
                 * 브라우저 formLogin과 일반 웹 요청에는
                 * Spring Security의 CSRF 보호를 유지한다.
                 *
                 * 현재 test.http와 HTTP Basic으로 호출하는 REST API만
                 * 임시로 CSRF 검사 대상에서 제외한다.
                 */
                .csrf(csrf ->
                        csrf.ignoringRequestMatchers(
                                "/api/**",
                                "/conversations/**",
                                "/permission/**"
                        )
                )

                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(
                                        "/",
                                        "/error",
                                        "/login",
                                        "/css/**",
                                        "/js/**",
                                        "/images/**"
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
                                .authenticated()
                )

                .formLogin(form ->
                        form
                                .successHandler(
                                        customAuthenticationSuccessHandler
                                )
                                .permitAll()
                )

                .httpBasic(
                        Customizer.withDefaults()
                )

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