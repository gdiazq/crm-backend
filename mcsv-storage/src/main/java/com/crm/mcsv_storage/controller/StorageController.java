package com.crm.mcsv_storage.controller;

import com.crm.mcsv_storage.dto.FileMetadataResponse;
import com.crm.mcsv_storage.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Storage", description = "Endpoints for file storage with AWS S3")
public class StorageController {

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file", description = "Upload a file to S3 and store metadata")
    public ResponseEntity<FileMetadataResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedBy") Long uploadedBy,
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "entityId", required = false) Long entityId,
            @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic) {
        FileMetadataResponse response = storageService.upload(file, uploadedBy, entityType, entityId, isPublic);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "Download file", description = "Generate presigned URL and redirect to S3")
    public ResponseEntity<Void> download(@PathVariable Long id) {
        String presignedUrl = storageService.getPresignedUrl(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete file", description = "Delete file from S3 and remove metadata")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam("userId") Long userId) {
        storageService.delete(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/files")
    @Operation(summary = "List files by entity", description = "List all files associated with an entity")
    public ResponseEntity<List<FileMetadataResponse>> listByEntity(
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId) {
        List<FileMetadataResponse> files = storageService.listByEntity(entityType, entityId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/files/{id}")
    @Operation(summary = "Get file metadata", description = "Get metadata for a specific file")
    public ResponseEntity<FileMetadataResponse> getById(@PathVariable Long id) {
        FileMetadataResponse response = storageService.getById(id);
        return ResponseEntity.ok(response);
    }
}
