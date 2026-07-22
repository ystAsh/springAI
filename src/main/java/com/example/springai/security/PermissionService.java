/*
 * =============================================================================
 * 클래스명 : PermissionService
 * =============================================================================
 * 목적
 *  - 사용자의 부서와 보안등급을 기준으로 접근 권한을 확인하는 클래스
 */

package com.example.springai.security;

import com.example.springai.entity.AppUser;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    /* 사용자가 해당 데이터에 접근 가능한지 확인한다. */
    public boolean canAccess(
            AppUser user,
            String departmentId,
            Integer securityLevel) {

        /* 같은 부서인지 확인 */
        boolean sameDepartment =
                user.getDepartmentId().equals(departmentId);

        /* 사용자 보안등급이 데이터 보안등급 이상인지 확인 */
        boolean allowedSecurityLevel =
                user.getSecurityLevel() >= securityLevel;

        return sameDepartment && allowedSecurityLevel;
    }
}