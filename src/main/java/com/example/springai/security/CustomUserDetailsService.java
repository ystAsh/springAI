/*
 * =============================================================================
 * 클래스명 : CustomUserDetailsService
 * =============================================================================
 * 목적
 *  - 로그인 아이디를 이용해 DB에서 사용자 정보를 조회한다.
 *  - 조회한 사용자를 Spring Security 인증 정보로 변환한다.
 */

package com.example.springai.security;

import com.example.springai.entity.AppUser;
import com.example.springai.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // 사용자 정보를 조회하는 Repository
    private final AppUserRepository appUserRepository;

    // AppUserRepository를 생성자 주입 방식으로 전달받는다.
    public CustomUserDetailsService(
            AppUserRepository appUserRepository
    ) {
        this.appUserRepository = appUserRepository;
    }

    // 로그인 아이디를 이용해 사용자 인증 정보를 반환한다.
    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        AppUser appUser =
                appUserRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "사용자를 찾을 수 없습니다: "
                                                + username
                                )
                        );

        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword())
                .roles(appUser.getRole())
                .build();
    }
}