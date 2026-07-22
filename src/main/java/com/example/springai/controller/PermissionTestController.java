/*
 * =============================================================================
 * 클래스명 : PermissionTestController
 * =============================================================================
 * 목적
 *  - 로그인 사용자의 데이터 접근 가능 여부를 테스트하는 Controller
 */

package com.example.springai.controller;

import com.example.springai.entity.AppUser;
import com.example.springai.security.CurrentUser;
import com.example.springai.security.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PermissionTestController {

    private final CurrentUser currentUser;
    private final PermissionService permissionService;

    public PermissionTestController(
            CurrentUser currentUser,
            PermissionService permissionService) {

        this.currentUser = currentUser;
        this.permissionService = permissionService;
    }

    /* 로그인 사용자의 접근 가능 여부를 반환한다. */
    @GetMapping("/permission/check")
    public Map<String, Object> checkPermission(
            @RequestParam String departmentId,
            @RequestParam Integer securityLevel) {

        /* 현재 로그인 사용자 조회 */
        AppUser user = currentUser.getCurrentUser();

        /* 접근 가능 여부 확인 */
        boolean allowed = permissionService.canAccess(
                user,
                departmentId,
                securityLevel
        );

        return Map.of(
                "username", user.getUsername(),
                "userDepartmentId", user.getDepartmentId(),
                "userSecurityLevel", user.getSecurityLevel(),
                "targetDepartmentId", departmentId,
                "targetSecurityLevel", securityLevel,
                "allowed", allowed
        );
    }
}