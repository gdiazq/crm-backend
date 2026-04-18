package com.crm.mcsv_user.storage;

import com.crm.common.dto.FileMetadataResponse;
import com.crm.common.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
@Tag(name = "Storage", description = "Endpoints for file storage with AWS S3")
public class StorageController {

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file")
    public ResponseEntity<FileMetadataResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedBy") Long uploadedBy,
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "entityId", required = false) Long entityId,
            @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storageService.upload(file, uploadedBy, entityType, entityId, isPublic));
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "Download file - presigned URL redirect")
    public ResponseEntity<Void> download(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(storageService.getPresignedUrl(id)))
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete file")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam("userId") Long userId) {
        storageService.delete(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/files")
    @Operation(summary = "List files by entity")
    public ResponseEntity<List<FileMetadataResponse>> listByEntity(
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId) {
        return ResponseEntity.ok(storageService.listByEntity(entityType, entityId));
    }

    @GetMapping("/files/{id}")
    @Operation(summary = "Get file metadata")
    public ResponseEntity<FileMetadataResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(storageService.getById(id));
    }

    @PatchMapping("/files/{id}/entity")
    @Operation(summary = "Retag file entity")
    public ResponseEntity<Void> retag(
            @PathVariable Long id,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId) {
        storageService.retag(id, entityType, entityId);
        return ResponseEntity.ok().build();
    }
}
