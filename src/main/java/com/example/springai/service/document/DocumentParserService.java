package com.example.springai.service.document;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class DocumentParserService {

    private final Tika tika =
            new Tika();

    /**
     * 업로드 문서에서 텍스트를 추출한다.
     */
    public String extractText(
            MultipartFile file
    ) {
        if (file == null
                || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "텍스트를 추출할 파일이 없습니다."
            );
        }

        try {
            String text =
                    tika.parseToString(
                            file.getInputStream()
                    );

            if (text == null
                    || text.isBlank()) {

                throw new IllegalArgumentException(
                        "문서에서 텍스트를 추출하지 못했습니다."
                );
            }

            return normalizeText(
                    text
            );

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

    /**
     * 파일의 실제 MIME Type을 감지한다.
     */
    public String detectContentType(
            MultipartFile file,
            String safeFileName
    ) {
        try {
            return tika.detect(
                    file.getInputStream(),
                    safeFileName
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "파일 형식을 확인하지 못했습니다.",
                    exception
            );
        }
    }

    private String normalizeText(
            String text
    ) {
        return text
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
    }
}