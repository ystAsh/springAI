/*
 * =============================================================================
 * 클래스명 : VectorDocumentService
 * =============================================================================
 * 목적
 *  - 업로드된 문서에서 텍스트를 추출하고 Chunk 단위로 분할한다.
 *  - 각 Chunk에 조직, 부서, 보안등급 등의 권한 메타데이터를 추가한다.
 *  - 생성된 Chunk와 메타데이터를 VectorStore에 저장한다.
 *  - 로그인 사용자의 권한 범위 안에서만 유사 문서를 검색한다.
 */

package com.example.springai.service;

import com.example.springai.dto.VectorDocumentUploadResponse;
import com.example.springai.dto.VectorSearchResponse;
import com.example.springai.entity.AppUser;
import com.example.springai.security.CurrentUser;
import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class VectorDocumentService {

    // 하나의 Chunk에 포함할 최대 문자 수
    private static final int CHUNK_SIZE = 800;

    // 인접한 Chunk 사이에 중복해서 포함할 문자 수
    private static final int CHUNK_OVERLAP = 100;

    // 업로드 가능한 최대 파일 크기인 10MB
    private static final long MAX_FILE_SIZE =
            10L * 1024L * 1024L;

    // 업로드를 허용하는 파일 확장자 목록
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "pdf",
                    "txt",
                    "doc",
                    "docx",
                    "ppt",
                    "pptx"
            );

    // 문서 Chunk와 Embedding 벡터를 저장하고 검색하는 객체
    private final VectorStore vectorStore;

    // 현재 로그인한 사용자의 DB 정보를 조회하는 객체
    private final CurrentUser currentUser;

    /*
     * =============================================================================
     * 생성자명 : VectorDocumentService
     * =============================================================================
     * 목적
     *  - VectorStore와 CurrentUser 객체를 생성자 주입 방식으로 전달받는다.
     */
    public VectorDocumentService(
            VectorStore vectorStore,
            CurrentUser currentUser
    ) {
        this.vectorStore = vectorStore;
        this.currentUser = currentUser;
    }

    /*
     * =============================================================================
     * 메서드명 : upload
     * =============================================================================
     */
    public VectorDocumentUploadResponse upload(
            MultipartFile file,
            Integer requestedSecurityLevel
    ) {

        // 현재 로그인한 사용자의 DB 정보를 조회한다.
        AppUser user = currentUser.getCurrentUser();

        // 업로드된 파일의 유효성을 검증한다.
        validateFile(file);

        // 문서에 적용할 보안등급을 결정한다.
        int documentSecurityLevel =
                resolveSecurityLevel(
                        user,
                        requestedSecurityLevel
                );

        // 업로드 파일명에서 경로 정보를 제거한다.
        String safeFileName =
                Paths.get(file.getOriginalFilename())
                        .getFileName()
                        .toString();

        // 업로드된 전체 문서를 구분할 고유 ID를 생성한다.
        String documentId =
                UUID.randomUUID().toString();

        // Apache Tika를 이용해 문서 텍스트를 추출한다.
        String extractedText =
                extractText(file);

        // 추출한 전체 텍스트를 Chunk 목록으로 분할한다.
        List<String> chunks =
                splitText(extractedText);

        // VectorStore에 저장할 Document 객체 목록을 생성한다.
        List<Document> documents =
                new ArrayList<>();

        // 생성된 모든 Chunk를 순서대로 처리한다.
        for (int index = 0;
             index < chunks.size();
             index++) {

            // 현재 순서의 Chunk 내용을 가져온다.
            String chunk =
                    chunks.get(index);

            // 현재 Chunk에 저장할 메타데이터 객체를 생성한다.
            Map<String, Object> metadata =
                    createMetadata(
                            user,
                            documentId,
                            safeFileName,
                            documentSecurityLevel,
                            index
                    );

            // VectorStore 내부에서 사용할 Chunk 고유 ID를 생성한다.
            String chunkId =
                    documentId + "-" + index;

            // Chunk 텍스트와 메타데이터를 가진 Document를 생성한다.
            Document document =
                    Document.builder()
                            .id(chunkId)
                            .text(chunk)
                            .metadata(metadata)
                            .build();

            // VectorStore 저장 대상 목록에 현재 Document를 추가한다.
            documents.add(document);
        }

        // 모든 Chunk의 Embedding을 생성하고 VectorStore에 저장한다.
        vectorStore.add(documents);

        // 문서 저장 처리 결과를 반환한다.
        return new VectorDocumentUploadResponse(
                documentId,
                safeFileName,
                documents.size(),
                user.getOrganizationId(),
                user.getDepartmentId(),
                documentSecurityLevel,
                user.getId(),
                "문서가 VectorStore에 저장되었습니다."
        );
    }

    /*
     * =============================================================================
     * 메서드명 : search
     * =============================================================================
     */
    public List<VectorSearchResponse> search(
            String query,
            Integer requestedTopK
    ) {

        // 현재 로그인한 사용자의 DB 정보를 조회한다.
        AppUser user = currentUser.getCurrentUser();

        // 검색 질문이 null이거나 빈 문자열인지 확인한다.
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "검색 질문은 비어 있을 수 없습니다."
            );
        }

        // 요청받은 topK를 안전한 범위로 변환한다.
        int topK =
                normalizeTopK(requestedTopK);

        // 현재 로그인 사용자에 맞는 권한 필터를 생성한다.
        String filterExpression =
                createFilterExpression(user);

        // 질문, 검색 개수, 유사도 및 권한 필터를 설정한다.
        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query.trim())
                        .topK(topK)
                        .similarityThreshold(0.0)
                        .filterExpression(filterExpression)
                        .build();

        // 질문과 의미가 유사하고 권한 필터를 통과한 문서를 검색한다.
        List<Document> searchResults =
                vectorStore.similaritySearch(
                        searchRequest
                );

        // VectorStore 구현체가 null을 반환하는 경우 빈 목록을 반환한다.
        if (searchResults == null) {
            return List.of();
        }

        // 검색된 Document 목록을 API 응답 DTO 목록으로 변환한다.
        return searchResults.stream()
                .map(document ->
                        new VectorSearchResponse(
                                document.getId(),
                                document.getText(),
                                document.getScore(),
                                document.getMetadata()
                        )
                )
                .toList();
    }

    /*
     * =============================================================================
     * 메서드명 : createMetadata
     * =============================================================================
     */
    private Map<String, Object> createMetadata(
            AppUser user,
            String documentId,
            String fileName,
            int securityLevel,
            int chunkIndex
    ) {

        // 메타데이터를 저장할 Map 객체를 생성한다.
        Map<String, Object> metadata =
                new HashMap<>();

        // 문서가 소속된 조직 ID를 저장한다.
        metadata.put(
                "organizationId",
                user.getOrganizationId()
        );

        // 문서가 소속된 부서 ID를 저장한다.
        metadata.put(
                "departmentId",
                user.getDepartmentId()
        );

        // 문서 접근에 필요한 보안등급을 저장한다.
        metadata.put(
                "securityLevel",
                securityLevel
        );

        // 업로드된 전체 문서를 구분하는 ID를 저장한다.
        metadata.put(
                "documentId",
                documentId
        );

        // 문서를 업로드한 사용자 ID를 저장한다.
        metadata.put(
                "ownerId",
                user.getId()
        );

        // 문서에 접근 가능한 사용자 역할을 저장한다.
        metadata.put(
                "allowedRoles",
                createAllowedRoles(user)
        );

        // 사용자가 업로드한 원본 파일명을 저장한다.
        metadata.put(
                "fileName",
                fileName
        );

        // 전체 문서에서 현재 Chunk가 몇 번째인지 저장한다.
        metadata.put(
                "chunkIndex",
                chunkIndex
        );

        return metadata;
    }

    /*
     * =============================================================================
     * 메서드명 : createFilterExpression
     * =============================================================================
     */
    private String createFilterExpression(
            AppUser user
    ) {

        // 필터식에 안전하게 사용할 조직 ID를 생성한다.
        String organizationId =
                escapeFilterValue(
                        user.getOrganizationId()
                );

        // 필터식에 안전하게 사용할 부서 ID를 생성한다.
        String departmentId =
                escapeFilterValue(
                        user.getDepartmentId()
                );

        // 사용자 역할이 null이어도 오류가 발생하지 않게 문자열로 변환한다.
        String role =
                user.getRole() == null
                        ? ""
                        : String.valueOf(
                        user.getRole()
                );

        // 관리자는 같은 조직의 허용된 보안등급 문서를 검색한다.
        if ("ADMIN".equalsIgnoreCase(role)) {
            return "organizationId == '"
                    + organizationId
                    + "' && securityLevel <= "
                    + user.getSecurityLevel();
        }

        // 일반 사용자는 같은 조직, 같은 부서, 허용된 보안등급만 검색한다.
        return "organizationId == '"
                + organizationId
                + "' && departmentId == '"
                + departmentId
                + "' && securityLevel <= "
                + user.getSecurityLevel();
    }

    /*
     * =============================================================================
     * 메서드명 : validateFile
     * =============================================================================
     */
    private void validateFile(
            MultipartFile file
    ) {

        // Multipart 요청에 파일 자체가 없는 경우를 차단한다.
        if (file == null) {
            throw new IllegalArgumentException(
                    "업로드 파일이 필요합니다."
            );
        }

        // 내용이 없는 빈 파일을 차단한다.
        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "빈 파일은 업로드할 수 없습니다."
            );
        }

        // 파일 크기가 10MB를 초과하면 차단한다.
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "파일 크기는 10MB를 초과할 수 없습니다."
            );
        }

        // 업로드된 원본 파일명을 가져온다.
        String originalFileName =
                file.getOriginalFilename();

        // 원본 파일명이 없거나 공백이면 차단한다.
        if (originalFileName == null
                || originalFileName.isBlank()) {

            throw new IllegalArgumentException(
                    "파일 이름이 존재하지 않습니다."
            );
        }

        // ../../secret.txt 같은 경로 정보를 제거한다.
        String safeFileName =
                Paths.get(originalFileName)
                        .getFileName()
                        .toString();

        // 안전하게 변환된 파일명에서 확장자를 추출한다.
        String extension =
                getExtension(safeFileName);

        // 허용된 확장자가 아니라면 업로드를 차단한다.
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "허용되지 않은 파일 형식입니다: "
                            + extension
            );
        }
    }

    /*
     * =============================================================================
     * 메서드명 : extractText
     * =============================================================================
     */
    private String extractText(
            MultipartFile file
    ) {

        try {
            // 파일 형식을 분석하고 텍스트를 추출하는 Tika 객체를 생성한다.
            Tika tika =
                    new Tika();

            // 업로드 파일의 InputStream에서 텍스트를 추출한다.
            String text =
                    tika.parseToString(
                            file.getInputStream()
                    );

            // 텍스트가 없거나 공백뿐인 문서를 차단한다.
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException(
                        "문서에서 텍스트를 추출하지 못했습니다."
                );
            }

            // 불필요한 앞뒤 공백을 제거해 반환한다.
            return text.trim();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "업로드 파일을 읽는 중 오류가 발생했습니다.",
                    exception
            );

        } catch (IllegalArgumentException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "문서 텍스트 추출 중 오류가 발생했습니다.",
                    exception
            );
        }
    }

    /*
     * =============================================================================
     * 메서드명 : splitText
     * =============================================================================
     */
    private List<String> splitText(
            String text
    ) {

        // 생성된 Chunk를 저장할 목록을 만든다.
        List<String> chunks =
                new ArrayList<>();

        // 현재 Chunk의 시작 위치를 0으로 설정한다.
        int start = 0;

        // 전체 문서의 끝에 도달할 때까지 반복한다.
        while (start < text.length()) {

            // 현재 시작 위치에서 최대 800자까지 끝 위치를 계산한다.
            int end =
                    Math.min(
                            start + CHUNK_SIZE,
                            text.length()
                    );

            // 문서 중간에서 끝나는 경우 문장 경계를 찾는다.
            if (end < text.length()) {
                int boundary =
                        findBoundary(
                                text,
                                start,
                                end
                        );

                // 사용 가능한 문장 경계를 찾았다면 끝 위치를 변경한다.
                if (boundary > start) {
                    end = boundary;
                }
            }

            // 시작 위치부터 끝 위치까지 문자열을 잘라낸다.
            String chunk =
                    text.substring(
                            start,
                            end
                    ).trim();

            // 공백이 아닌 Chunk만 결과 목록에 추가한다.
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            // 현재 Chunk가 문서 끝까지 포함했다면 반복을 종료한다.
            if (end >= text.length()) {
                break;
            }

            // 다음 Chunk 시작 위치를 현재 끝보다 100자 앞쪽으로 설정한다.
            int nextStart =
                    end - CHUNK_OVERLAP;

            // 다음 시작 위치가 현재 위치보다 앞으로 진행되도록 보장한다.
            start =
                    Math.max(
                            nextStart,
                            start + 1
                    );
        }

        // 하나의 Chunk도 생성하지 못한 경우 오류를 발생시킨다.
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "문서 Chunk를 생성하지 못했습니다."
            );
        }

        return chunks;
    }

    /*
     * =============================================================================
     * 메서드명 : findBoundary
     * =============================================================================
     */
    private int findBoundary(
            String text,
            int start,
            int end
    ) {

        // 현재 끝 위치 이전의 마지막 줄바꿈 위치를 찾는다.
        int newlineIndex =
                text.lastIndexOf(
                        '\n',
                        end - 1
                );

        // 현재 끝 위치 이전의 마지막 마침표 위치를 찾는다.
        int periodIndex =
                text.lastIndexOf(
                        '.',
                        end - 1
                );

        // 현재 끝 위치 이전의 마지막 물음표 위치를 찾는다.
        int questionIndex =
                text.lastIndexOf(
                        '?',
                        end - 1
                );

        // 현재 끝 위치 이전의 마지막 느낌표 위치를 찾는다.
        int exclamationIndex =
                text.lastIndexOf(
                        '!',
                        end - 1
                );

        // 발견한 문장 경계 중 가장 뒤쪽 위치를 선택한다.
        int boundary =
                Math.max(
                        Math.max(
                                newlineIndex,
                                periodIndex
                        ),
                        Math.max(
                                questionIndex,
                                exclamationIndex
                        )
                );

        // 너무 작은 Chunk가 만들어지지 않도록 최소 경계를 계산한다.
        int minimumBoundary =
                start + (CHUNK_SIZE / 2);

        // 최소 경계 이후의 문장 끝을 찾았다면 그 다음 위치를 반환한다.
        if (boundary >= minimumBoundary) {
            return boundary + 1;
        }

        // 적절한 문장 경계가 없으면 기존 최대 끝 위치를 반환한다.
        return end;
    }

    /*
     * =============================================================================
     * 메서드명 : resolveSecurityLevel
     * =============================================================================
     */
    private int resolveSecurityLevel(
            AppUser user,
            Integer requestedSecurityLevel
    ) {

        // 요청값이 없으면 현재 사용자의 보안등급을 적용한다.
        int securityLevel =
                requestedSecurityLevel == null
                        ? user.getSecurityLevel()
                        : requestedSecurityLevel;

        // 문서 보안등급이 1부터 5 사이인지 확인한다.
        if (securityLevel < 1
                || securityLevel > 5) {

            throw new IllegalArgumentException(
                    "보안등급은 1부터 5 사이여야 합니다."
            );
        }

        // 사용자 역할을 null에 안전한 문자열로 변환한다.
        String role =
                user.getRole() == null
                        ? ""
                        : String.valueOf(
                        user.getRole()
                );

        // 일반 사용자가 자신의 등급보다 높은 문서를 등록하지 못하게 한다.
        if (!"ADMIN".equalsIgnoreCase(role)
                && securityLevel > user.getSecurityLevel()) {

            throw new SecurityException(
                    "자신의 보안등급보다 높은 문서를 등록할 수 없습니다."
            );
        }

        return securityLevel;
    }

    /*
     * =============================================================================
     * 메서드명 : createAllowedRoles
     * =============================================================================
     */
    private String createAllowedRoles(
            AppUser user
    ) {

        // 사용자 역할을 null에 안전한 문자열로 변환한다.
        String role =
                user.getRole() == null
                        ? "USER"
                        : String.valueOf(
                        user.getRole()
                ).toUpperCase();

        // 관리자가 등록한 문서는 관리자 역할로 저장한다.
        if ("ADMIN".equals(role)) {
            return "ADMIN";
        }

        // 일반 사용자 문서는 해당 역할과 관리자가 접근할 수 있게 저장한다.
        return role + ",ADMIN";
    }

    /*
     * =============================================================================
     * 메서드명 : normalizeTopK
     * =============================================================================
     */
    private int normalizeTopK(
            Integer requestedTopK
    ) {

        // 요청값이 없으면 기본 검색 결과 개수 5를 반환한다.
        if (requestedTopK == null) {
            return 5;
        }

        // topK가 1보다 작거나 20보다 크면 차단한다.
        if (requestedTopK < 1
                || requestedTopK > 20) {

            throw new IllegalArgumentException(
                    "topK는 1부터 20 사이여야 합니다."
            );
        }

        return requestedTopK;
    }

    /*
     * =============================================================================
     * 메서드명 : getExtension
     * =============================================================================
     */
    private String getExtension(
            String fileName
    ) {

        // 파일명에서 마지막 마침표 위치를 찾는다.
        int dotIndex =
                fileName.lastIndexOf('.');

        // 마침표가 없거나 마지막 글자인 경우 빈 문자열을 반환한다.
        if (dotIndex < 0
                || dotIndex == fileName.length() - 1) {

            return "";
        }

        // 마지막 마침표 다음 문자열을 소문자로 변환한다.
        return fileName
                .substring(dotIndex + 1)
                .toLowerCase();
    }

    /*
     * =============================================================================
     * 메서드명 : escapeFilterValue
     * =============================================================================
     */
    private String escapeFilterValue(
            String value
    ) {

        // 필수 권한 정보가 없는 경우 검색을 중단한다.
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "사용자 권한 정보가 올바르지 않습니다."
            );
        }

        // 역슬래시와 작은따옴표를 이스케이프 처리한다.
        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'");
    }
}