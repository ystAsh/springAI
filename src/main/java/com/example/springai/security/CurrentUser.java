/*
 * =============================================================================
 * 클래스명 : CurrentUser
 * =============================================================================
 * 목적
 *  - 현재 로그인한 사용자의 정보를 조회하는 클래스
 */

package com.example.springai.security;

import com.example.springai.entity.AppUser;
import com.example.springai.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final AppUserRepository appUserRepository;

    public CurrentUser(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    /* 현재 로그인한 사용자 정보를 반환한다. */
    public AppUser getCurrentUser() {

        /* 현재 로그인한 사용자 정보 */
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        /* 로그인 아이디 */
        String username = authentication.getName();

        /* DB에서 사용자 조회 */
        return appUserRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("로그인 사용자를 찾을 수 없습니다."));
    }

}