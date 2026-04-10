package com.crm.mcsv_rrhh.util;

import com.crm.mcsv_rrhh.client.StorageClient;
import com.crm.common.dto.FileMetadataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FileUploadHelper {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png");

    private final StorageClient storageClient;

    public List<FileMetadataResponse> uploadFiles(List<MultipartFile> files, Long uploadedBy,
                                                   String entityType, Long entityId) {
        if (files == null || files.isEmpty()) return Collections.emptyList();
        List<FileMetadataResponse> uploaded = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
            ResponseEntity<FileMetadataResponse> response =
                    storageClient.upload(file, uploadedBy, entityType, entityId, false);
            if (response.getBody() != null) uploaded.add(response.getBody());
        }
        return uploaded;
    }

    public void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE)
            throw new IllegalArgumentException(
                    "El archivo '" + file.getOriginalFilename() + "' supera el límite de 10MB.");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new IllegalArgumentException(
                    "El archivo '" + file.getOriginalFilename() + "' no es un formato permitido. Use PDF, JPG o PNG.");
    }
}
