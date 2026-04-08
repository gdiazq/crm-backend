package com.crm.mcsv_rrhh.client;

import com.crm.mcsv_rrhh.dto.FileMetadataResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "mcsv-storage", fallback = com.crm.mcsv_rrhh.client.fallback.StorageClientFallback.class)
public interface StorageClient {

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<FileMetadataResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("uploadedBy") Long uploadedBy,
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "entityId", required = false) Long entityId,
            @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @RequestParam("userId") Long userId);

    @GetMapping("/files")
    ResponseEntity<List<FileMetadataResponse>> listByEntity(
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId);

    @PatchMapping("/files/{id}/entity")
    ResponseEntity<Void> retag(
            @PathVariable("id") Long id,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId);
}
