/*
 * =============================================================================
 * 클래스명 : DocumentService
 * =============================================================================
 * 목적
 *  - 업로드된 파일의 이름, 크기, 확장자를 검증한다.
 *  - TikaDocumentReader를 사용하여 문서의 텍스트를 추출한다.
 *  - 추출 결과를 DocumentUploadResponse로 반환한다.
 */

package com.example.springai.service;

import com.example.springai.dto.DocumentUploadResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    // 현재 단계에서 허용할 파일 확장자 목록
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "txt", "doc", "docx", "ppt", "pptx");

    // 업로드 가능한 최대 파일 크기: 10MB
    private static final long MAX_FILE_SIZE =
            10L * 1024L * 1024L;

    /*
     * 업로드된 파일을 검증하고,
     * 문서에서 텍스트를 추출한다.
     */
    public DocumentUploadResponse extractText(
            MultipartFile file
    ) {
        // 업로드된 파일 유효성 검사
        validateFile(file);

        try {
            // MultipartFile을 읽을 TikaDocumentReader 생성
            TikaDocumentReader documentReader =
                    new TikaDocumentReader(file.getResource());

            // 문서를 Spring AI Document 목록으로 변환
            List<Document> documents =
                    documentReader.read();

            // 각 Document의 텍스트를 하나의 문자열로 결합
            String extractedText =
                    documents.stream()
                            .map(Document::getText)
                            .filter(text ->
                                    text != null && !text.isBlank()
                            )
                            .collect(Collectors.joining(
                                    System.lineSeparator()
                                            + System.lineSeparator()
                            ));

            // 문서에서 텍스트가 추출되지 않은 경우
            if (extractedText.isBlank()) {
                throw new IllegalArgumentException(
                        "문서에서 추출된 텍스트가 없습니다. "
                                + "이미지로만 구성된 PDF인지 확인해 주세요."
                );
            }

            // 추출 결과를 응답 DTO로 변환
            return new DocumentUploadResponse(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    documents.size(),
                    extractedText.length(),
                    extractedText
            );

        } catch (IllegalArgumentException exception) {
            // 직접 발생시킨 검증 예외는 그대로 전달
            throw exception;

        } catch (Exception exception) {
            // 문서 처리 중 발생한 예외를 사용자 메시지로 변환
            throw new IllegalArgumentException(
                    "문서를 읽는 중 오류가 발생했습니다: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    /*
     * 업로드된 파일의 존재 여부, 크기,
     * 파일 이름과 확장자를 검증한다.
     */
    private void validateFile(
            MultipartFile file
    ) {
        // multipart 요청에 파일이 포함되었는지 검사
        if (file == null) {
            throw new IllegalArgumentException(
                    "업로드할 파일이 없습니다."
            );
        }

        // 업로드된 파일이 비어 있는지 검사
        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "빈 파일은 업로드할 수 없습니다."
            );
        }

        // 최대 파일 크기인 10MB를 초과하는지 검사
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "파일 크기는 10MB를 초과할 수 없습니다."
            );
        }

        // 업로드된 원본 파일 이름 조회
        String originalFilename =
                file.getOriginalFilename();

        // 원본 파일 이름이 존재하는지 검사
        if (originalFilename == null
                || originalFilename.isBlank()) {

            throw new IllegalArgumentException(
                    "파일 이름을 확인할 수 없습니다."
            );
        }

        // 원본 파일 이름에서 확장자 추출
        String extension =
                getExtension(originalFilename);

        // 허용된 확장자인지 검사
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "허용되지 않는 파일 형식입니다. "
                            + "허용 형식: PDF, TXT, DOC, DOCX, PPT, PPTX"
            );
        }
    }

    /*
     * 파일 이름에서 마지막 확장자를 추출하고,
     * 소문자로 변환하여 반환한다.
     */
    private String getExtension(
            String filename
    ) {
        // 파일 이름에서 마지막 점의 위치 조회
        int lastDotIndex =
                filename.lastIndexOf('.');

        // 점이 없거나 마지막 문자가 점인 경우
        if (lastDotIndex < 0
                || lastDotIndex == filename.length() - 1) {

            throw new IllegalArgumentException(
                    "파일 확장자가 없습니다."
            );
        }

        // 점 다음 문자열을 소문자로 변환하여 반환
        return filename
                .substring(lastDotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }
}