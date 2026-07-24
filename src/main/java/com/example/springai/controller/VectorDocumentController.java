/*
 * =============================================================================
 * 클래스명 : VectorDocumentController
 * =============================================================================
 * 목적
 *  - 문서 파일을 업로드하여 VectorStore에 저장하는 API를 제공한다.
 *  - 로그인 사용자의 권한 범위 안에서 유사 문서를 검색하는 API를 제공한다.
 */

package com.example.springai.controller;

import com.example.springai.dto.VectorDocumentUploadResponse;
import com.example.springai.dto.VectorSearchResponse;
import com.example.springai.service.VectorDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/vector-documents")
public class VectorDocumentController {

    // 문서 저장과 검색 기능을 처리하는 Service
    private final VectorDocumentService vectorDocumentService;

    // VectorDocumentService를 생성자 주입 방식으로 전달받는다.
    public VectorDocumentController(
            VectorDocumentService vectorDocumentService
    ) {
        this.vectorDocumentService = vectorDocumentService;
    }

    // 업로드된 문서를 Chunk로 분할하여 VectorStore에 저장한다.
    @PostMapping("/upload")
    public ResponseEntity<VectorDocumentUploadResponse> upload(
            @RequestParam("file")
            MultipartFile file,

            @RequestParam(
                    name = "securityLevel",
                    required = false
            )
            Integer securityLevel
    ) {

        VectorDocumentUploadResponse response =
                vectorDocumentService.upload(
                        file,
                        securityLevel
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 로그인 사용자의 권한 범위 안에서 유사 문서를 검색한다.
    @GetMapping("/search")
    public ResponseEntity<List<VectorSearchResponse>> search(
            @RequestParam("query")
            String query,

            @RequestParam(
                    name = "topK",
                    required = false,
                    defaultValue = "5"
            )
            Integer topK
    ) {

        List<VectorSearchResponse> responses =
                vectorDocumentService.search(
                        query,
                        topK
                );

        return ResponseEntity.ok(responses);
    }
}