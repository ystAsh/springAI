/*
 * =============================================================================
 * 클래스명 : UserController
 * =============================================================================
 * 목적
 *  - 현재 로그인한 사용자 정보를 반환하는 Controller
 */

package com.example.springai.controller;

import com.example.springai.entity.AppUser;
import com.example.springai.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    private final CurrentUser currentUser;
    /* CurrentUser 객체를 자동으로 주입 */
    public UserController(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    /* 현재 로그인한 사용자 정보를 반환한다. */
    @GetMapping("/me")
    public Map<String, Object> me() {

        /* 현재 로그인한 사용자 조회 */
        AppUser user = currentUser.getCurrentUser();

        /* 사용자 정보를 JSON으로 반환 */
        return Map.of(
                "username", user.getUsername(),
                "role", user.getRole(),
                "organizationId", user.getOrganizationId(),
                "departmentId", user.getDepartmentId(),
                "securityLevel", user.getSecurityLevel()
        );
    }
}