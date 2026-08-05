package com.example.springai.service.document;

import com.example.springai.dto.VectorDocumentUploadResponse;
import com.example.springai.entity.AppUser;
import com.example.springai.security.CurrentUser;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentUploadService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    DocumentUploadService.class
            );

    private static final long MAX_FILE_SIZE =
            10L * 1024L * 1024L;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "pdf",
                    "txt",
                    "doc",
                    "docx",
                    "ppt",
                    "pptx"
            );

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "application/pdf",
                    "text/plain",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            );

    private final CurrentUser currentUser;
    private final DocumentParserService documentParserService;
    private final DocumentChunkService documentChunkService;
    private final MetadataService metadataService;
    private final VectorStoreService vectorStoreService;
    private final Path uploadRootDirectory;

    public DocumentUploadService(
            CurrentUser currentUser,
            DocumentParserService documentParserService,
            DocumentChunkService documentChunkService,
            MetadataService metadataService,
            VectorStoreService vectorStoreService,
            @Value("${app.upload.vector-document-directory}")
            String uploadDirectory
    ) {
        this.currentUser =
                currentUser;

        this.documentParserService =
                documentParserService;

        this.documentChunkService =
                documentChunkService;

        this.metadataService =
                metadataService;

        this.vectorStoreService =
                vectorStoreService;

        this.uploadRootDirectory =
                Path.of(
                                uploadDirectory
                        )
                        .toAbsolutePath()
                        .normalize();
    }

    public VectorDocumentUploadResponse upload(
            MultipartFile file,
            Integer requestedSecurityLevel
    ) {
        AppUser user =
                currentUser.getCurrentUser();

        metadataService.validateUserSecurityScope(
                user
        );

        String safeFileName =
                validateAndSanitizeFile(
                        file
                );

        int documentSecurityLevel =
                metadataService
                        .resolveDocumentSecurityLevel(
                                user,
                                requestedSecurityLevel
                        );

        /*
         * 추후 MSSQL vector_documents.id로 교체한다.
         */
        String documentId =
                UUID.randomUUID()
                        .toString();

        Path storedFilePath =
                saveOriginalFile(
                        file,
                        user,
                        documentId,
                        safeFileName
                );

        try {
            String extractedText =
                    documentParserService
                            .extractText(
                                    file
                            );

            List<String> chunks =
                    documentChunkService
                            .split(
                                    extractedText
                            );

            List<Document> documents =
                    createDocuments(
                            chunks,
                            user,
                            documentId,
                            safeFileName,
                            storedFilePath,
                            documentSecurityLevel
                    );

            vectorStoreService.addDocuments(
                    documents
            );

            log.info(
                    "VectorStore 문서 저장 완료. documentId={}, chunkCount={}",
                    documentId,
                    documents.size()
            );

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

        } catch (RuntimeException exception) {
            deleteStoredFileQuietly(
                    storedFilePath
            );

            throw exception;
        }
    }

    private List<Document> createDocuments(
            List<String> chunks,
            AppUser user,
            String documentId,
            String safeFileName,
            Path storedFilePath,
            int documentSecurityLevel
    ) {
        List<Document> documents =
                new ArrayList<>(
                        chunks.size()
                );

        for (int index = 0;
             index < chunks.size();
             index++) {

            String chunk =
                    chunks.get(index);

            Map<String, Object> metadata =
                    metadataService
                            .createChunkMetadata(
                                    user,
                                    documentId,
                                    safeFileName,
                                    storedFilePath,
                                    documentSecurityLevel,
                                    index
                            );

            String chunkId =
                    documentId
                            + "-"
                            + index;

            Document document =
                    Document.builder()
                            .id(
                                    chunkId
                            )
                            .text(
                                    chunk
                            )
                            .metadata(
                                    metadata
                            )
                            .build();

            documents.add(
                    document
            );
        }

        return documents;
    }

    private String validateAndSanitizeFile(
            MultipartFile file
    ) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "업로드 파일이 필요합니다."
            );
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "빈 파일은 업로드할 수 없습니다."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "파일 크기는 10MB를 초과할 수 없습니다."
            );
        }

        String safeFileName =
                sanitizeFileName(
                        file.getOriginalFilename()
                );

        String extension =
                getExtension(
                        safeFileName
                );

        if (!ALLOWED_EXTENSIONS.contains(
                extension
        )) {
            throw new IllegalArgumentException(
                    "허용되지 않은 파일 확장자입니다: "
                            + extension
            );
        }

        String detectedContentType =
                documentParserService
                        .detectContentType(
                                file,
                                safeFileName
                        );

        if (detectedContentType == null
                || !ALLOWED_CONTENT_TYPES.contains(
                detectedContentType
        )) {
            throw new IllegalArgumentException(
                    "허용되지 않은 파일 내용 형식입니다: "
                            + detectedContentType
            );
        }

        return safeFileName;
    }

    private Path saveOriginalFile(
            MultipartFile file,
            AppUser user,
            String documentId,
            String safeFileName
    ) {
        try {
            String organizationDirectoryName =
                    sanitizeDirectoryName(
                            user.getOrganizationId()
                    );

            String departmentDirectoryName =
                    sanitizeDirectoryName(
                            user.getDepartmentId()
                    );

            String documentDirectoryName =
                    sanitizeDirectoryName(
                            documentId
                    );

            Path documentDirectory =
                    uploadRootDirectory
                            .resolve(
                                    organizationDirectoryName
                            )
                            .resolve(
                                    departmentDirectoryName
                            )
                            .resolve(
                                    documentDirectoryName
                            )
                            .normalize();

            validatePathInsideUploadRoot(
                    documentDirectory
            );

            Files.createDirectories(
                    documentDirectory
            );

            Path targetPath =
                    documentDirectory
                            .resolve(
                                    safeFileName
                            )
                            .normalize();

            validatePathInsideUploadRoot(
                    targetPath
            );

            if (!targetPath.startsWith(
                    documentDirectory
            )) {
                throw new SecurityException(
                        "올바르지 않은 파일 저장 경로입니다."
                );
            }

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            log.info(
                    "원본 문서 저장 완료. documentId={}, fileName={}",
                    documentId,
                    safeFileName
            );

            return targetPath;

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "업로드 파일을 폴더에 저장하지 못했습니다.",
                    exception
            );
        }
    }

    private void deleteStoredFileQuietly(
            Path storedFilePath
    ) {
        if (storedFilePath == null) {
            return;
        }

        try {
            Files.deleteIfExists(
                    storedFilePath
            );

            Path documentDirectory =
                    storedFilePath.getParent();

            if (documentDirectory != null
                    && !documentDirectory.equals(
                    uploadRootDirectory
            )) {
                Files.deleteIfExists(
                        documentDirectory
                );
            }

        } catch (IOException exception) {
            log.error(
                    "실패한 문서의 원본 파일을 삭제하지 못했습니다. path={}",
                    storedFilePath,
                    exception
            );
        }
    }

    private String sanitizeFileName(
            String originalFileName
    ) {
        if (originalFileName == null
                || originalFileName.isBlank()) {

            throw new IllegalArgumentException(
                    "파일 이름이 존재하지 않습니다."
            );
        }

        String fileName =
                Paths.get(
                                originalFileName
                        )
                        .getFileName()
                        .toString()
                        .trim();

        String sanitizedFileName =
                fileName.replaceAll(
                        "[\\\\/:*?\"<>|\\p{Cntrl}]",
                        "_"
                );

        sanitizedFileName =
                sanitizedFileName.replaceAll(
                        "[.\\s]+$",
                        ""
                );

        if (sanitizedFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "올바른 파일 이름이 아닙니다."
            );
        }

        return sanitizedFileName;
    }

    private String getExtension(
            String fileName
    ) {
        int dotIndex =
                fileName.lastIndexOf(
                        '.'
                );

        if (dotIndex < 0
                || dotIndex == fileName.length() - 1) {

            return "";
        }

        return fileName
                .substring(
                        dotIndex + 1
                )
                .toLowerCase();
    }

    private String sanitizeDirectoryName(
            String value
    ) {
        if (value == null
                || value.isBlank()) {

            throw new IllegalStateException(
                    "문서 저장에 필요한 경로 정보가 없습니다."
            );
        }

        String sanitizedValue =
                value.trim()
                        .replaceAll(
                                "[^a-zA-Z0-9_-]",
                                "_"
                        );

        if (sanitizedValue.isBlank()) {
            throw new IllegalStateException(
                    "올바르지 않은 문서 저장 경로 정보입니다."
            );
        }

        return sanitizedValue;
    }

    private void validatePathInsideUploadRoot(
            Path path
    ) {
        if (!path.startsWith(
                uploadRootDirectory
        )) {
            throw new SecurityException(
                    "허용된 업로드 폴더 밖으로 파일을 저장할 수 없습니다."
            );
        }
    }
}