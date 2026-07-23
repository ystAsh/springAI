/*
 * =============================================================================
 * 클래스명 : DocumentController
 * =============================================================================
 * 목적
 *  - 문서 업로드 API를 제공한다.
 *  - 업로드된 파일을 DocumentService에 전달한다.
 *  - 문서에서 추출된 텍스트 결과를 반환한다.
 */

package com.example.springai.controller;

import com.example.springai.dto.DocumentUploadResponse;
import com.example.springai.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(
            DocumentService documentService
    ) {
        this.documentService = documentService;
    }

    /*
     * 문서를 업로드하고,
     * 문서에서 추출된 텍스트를 반환한다.
     */
    @PostMapping("/extract")
    @ResponseStatus(HttpStatus.OK)
    public DocumentUploadResponse extractText(
            @RequestParam("file") MultipartFile file
    ) {
        // 업로드된 파일을 Service에 전달
        return documentService.extractText(file);
    }
}