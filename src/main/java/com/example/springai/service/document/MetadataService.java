package com.example.springai.service.document;

import com.example.springai.entity.AppUser;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
public class MetadataService {

    /**
     * PGVector에 저장할 문서 Chunk 메타데이터를 생성한다.
     */
    public Map<String, Object> createChunkMetadata(
            AppUser user,
            String documentId,
            String fileName,
            Path storedFilePath,
            int documentSecurityLevel,
            int chunkIndex
    ) {
        validateUserSecurityScope(
                user
        );

        Map<String, Object> metadata =
                new HashMap<>();

        metadata.put(
                "documentId",
                documentId
        );

        metadata.put(
                "organizationId",
                user.getOrganizationId()
        );

        metadata.put(
                "departmentId",
                user.getDepartmentId()
        );

        metadata.put(
                "securityLevel",
                documentSecurityLevel
        );

        metadata.put(
                "ownerId",
                user.getId()
        );

        metadata.put(
                "allowedRoles",
                createAllowedRoles(
                        user
                )
        );

        metadata.put(
                "fileName",
                fileName
        );

        metadata.put(
                "chunkIndex",
                chunkIndex
        );

        /*
         * 내부 관리용 경로다.
         * API 응답으로는 공개하지 않는다.
         */
        metadata.put(
                "storedFilePath",
                storedFilePath.toString()
        );

        return metadata;
    }

    /**
     * 검색 API에서 외부에 공개할 메타데이터만 반환한다.
     */
    public Map<String, Object> createPublicMetadata(
            Map<String, Object> sourceMetadata
    ) {
        Map<String, Object> publicMetadata =
                new HashMap<>();

        if (sourceMetadata == null
                || sourceMetadata.isEmpty()) {

            return publicMetadata;
        }

        copyIfPresent(
                sourceMetadata,
                publicMetadata,
                "documentId"
        );

        copyIfPresent(
                sourceMetadata,
                publicMetadata,
                "fileName"
        );

        copyIfPresent(
                sourceMetadata,
                publicMetadata,
                "chunkIndex"
        );

        copyIfPresent(
                sourceMetadata,
                publicMetadata,
                "securityLevel"
        );

        copyIfPresent(
                sourceMetadata,
                publicMetadata,
                "documentType"
        );

        copyIfPresent(
                sourceMetadata,
                publicMetadata,
                "distance"
        );

        return publicMetadata;
    }

    /**
     * 로그인 사용자 권한을 이용해 PGVector 검색 필터를 생성한다.
     */
    public String createSecurityFilter(
            AppUser user
    ) {
        validateUserSecurityScope(
                user
        );

        String organizationId =
                escapeFilterValue(
                        user.getOrganizationId()
                );

        String departmentId =
                escapeFilterValue(
                        user.getDepartmentId()
                );

        String role =
                normalizeRole(
                        user.getRole()
                );

        /*
         * 관리자는 동일 조직 안에서 부서 제한 없이 검색한다.
         */
        if ("ADMIN".equals(role)) {
            return "organizationId == '"
                    + organizationId
                    + "' && securityLevel <= "
                    + user.getSecurityLevel();
        }

        /*
         * 일반 사용자는 동일 조직, 동일 부서 문서만 검색한다.
         */
        return "organizationId == '"
                + organizationId
                + "' && departmentId == '"
                + departmentId
                + "' && securityLevel <= "
                + user.getSecurityLevel();
    }

    /**
     * 업로드 문서에 적용할 보안등급을 결정한다.
     */
    public int resolveDocumentSecurityLevel(
            AppUser user,
            Integer requestedSecurityLevel
    ) {
        validateUserSecurityScope(
                user
        );

        int userSecurityLevel =
                user.getSecurityLevel();

        int documentSecurityLevel =
                requestedSecurityLevel == null
                        ? userSecurityLevel
                        : requestedSecurityLevel;

        if (documentSecurityLevel < 1
                || documentSecurityLevel > 5) {

            throw new IllegalArgumentException(
                    "보안등급은 1부터 5 사이여야 합니다."
            );
        }

        String role =
                normalizeRole(
                        user.getRole()
                );

        if (!"ADMIN".equals(role)
                && documentSecurityLevel > userSecurityLevel) {

            throw new SecurityException(
                    "자신의 보안등급보다 높은 문서를 등록할 수 없습니다."
            );
        }

        return documentSecurityLevel;
    }

    public void validateUserSecurityScope(
            AppUser user
    ) {
        if (user == null) {
            throw new IllegalStateException(
                    "로그인 사용자 정보를 확인할 수 없습니다."
            );
        }

        if (user.getId() == null) {
            throw new IllegalStateException(
                    "로그인 사용자 ID가 없습니다."
            );
        }

        if (user.getOrganizationId() == null
                || user.getOrganizationId().isBlank()) {

            throw new IllegalStateException(
                    "로그인 사용자의 조직 정보가 없습니다."
            );
        }

        if (user.getDepartmentId() == null
                || user.getDepartmentId().isBlank()) {

            throw new IllegalStateException(
                    "로그인 사용자의 부서 정보가 없습니다."
            );
        }

        Integer securityLevel =
                user.getSecurityLevel();

        if (securityLevel == null
                || securityLevel < 1
                || securityLevel > 5) {

            throw new IllegalStateException(
                    "로그인 사용자의 보안등급이 올바르지 않습니다."
            );
        }
    }

    private String createAllowedRoles(
            AppUser user
    ) {
        String role =
                normalizeRole(
                        user.getRole()
                );

        if ("ADMIN".equals(role)) {
            return "ADMIN";
        }

        return role + ",ADMIN";
    }

    private String normalizeRole(
            Object role
    ) {
        if (role == null) {
            return "USER";
        }

        String normalizedRole =
                String.valueOf(
                                role
                        )
                        .trim()
                        .toUpperCase();

        if (normalizedRole.startsWith(
                "ROLE_"
        )) {
            normalizedRole =
                    normalizedRole.substring(
                            "ROLE_".length()
                    );
        }

        if (normalizedRole.isBlank()) {
            return "USER";
        }

        return normalizedRole;
    }

    private String escapeFilterValue(
            String value
    ) {
        if (value == null
                || value.isBlank()) {

            throw new IllegalStateException(
                    "사용자 권한 정보가 올바르지 않습니다."
            );
        }

        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "'",
                        "\\'"
                );
    }

    private void copyIfPresent(
            Map<String, Object> source,
            Map<String, Object> target,
            String key
    ) {
        if (source.containsKey(
                key
        )) {
            target.put(
                    key,
                    source.get(key)
            );
        }
    }
}