/*
 * =============================================================================
 * 클래스명 : CurrentUser
 * =============================================================================
 * 목적
 *  - Spring Security에서 현재 로그인한 사용자의 아이디를 조회한다.
 *  - 로그인 아이디를 이용해 DB에 저장된 AppUser 정보를 반환한다.
 *  - 인증되지 않은 사용자의 접근을 차단한다.
 */

package com.example.springai.security;

import com.example.springai.entity.AppUser;
import com.example.springai.repository.AppUserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    // 사용자 정보를 조회하는 Repository
    private final AppUserRepository appUserRepository;

    // AppUserRepository를 생성자 주입 방식으로 전달받는다.
    public CurrentUser(
            AppUserRepository appUserRepository
    ) {
        this.appUserRepository = appUserRepository;
    }

    // 현재 로그인한 사용자 정보를 반환한다.
    public AppUser getCurrentUser() {

        // Spring Security가 관리하는 현재 인증 정보를 조회한다.
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        // 인증 정보가 없는 경우 요청을 차단한다.
        if (authentication == null) {
            throw new IllegalStateException(
                    "인증 정보가 존재하지 않습니다."
            );
        }

        // 인증이 완료되지 않은 사용자의 요청을 차단한다.
        if (!authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "인증되지 않은 사용자입니다."
            );
        }

        // 익명 사용자 요청을 차단한다.
        if (authentication
                instanceof AnonymousAuthenticationToken) {

            throw new IllegalStateException(
                    "로그인이 필요한 기능입니다."
            );
        }

        // 인증 객체에서 로그인 아이디를 가져온다.
        String username =
                authentication.getName();

        // 로그인 아이디가 없거나 비어 있는 경우 요청을 차단한다.
        if (username == null || username.isBlank()) {
            throw new IllegalStateException(
                    "로그인 아이디를 확인할 수 없습니다."
            );
        }

        // 로그인 아이디를 이용해 DB에서 사용자 정보를 조회한다.
        return appUserRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "로그인 사용자 정보를 DB에서 찾을 수 없습니다: "
                                        + username
                        )
                );
    }
}