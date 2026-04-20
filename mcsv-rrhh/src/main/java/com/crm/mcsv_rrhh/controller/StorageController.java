package com.crm.mcsv_rrhh.controller;

import com.crm.common.dto.FileMetadataResponse;
import com.crm.common.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
@Tag(name = "Storage RRHH", description = "Descarga y consulta de archivos adjuntos de módulos RRHH")
public class StorageController {

    private final StorageService storageService;

    @GetMapping("/download/{fileId}")
    @Operation(summary = "Descargar archivo - redirige a URL prefirmada de S3")
    public ResponseEntity<Void> download(@PathVariable Long fileId) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(storageService.getPresignedUrl(fileId)))
                .build();
    }

    @GetMapping("/presigned/{fileId}")
    @Operation(summary = "Obtener URL prefirmada de S3 (sin redirect)")
    public ResponseEntity<String> presignedUrl(@PathVariable Long fileId) {
        return ResponseEntity.ok(storageService.getPresignedUrl(fileId));
    }

    @GetMapping("/files/{fileId}")
    @Operation(summary = "Metadata de un archivo")
    public ResponseEntity<FileMetadataResponse> getById(@PathVariable Long fileId) {
        return ResponseEntity.ok(storageService.getById(fileId));
    }

    @GetMapping("/files")
    @Operation(summary = "Listar archivos por entidad (CONTRACT, TRANSFER, SETTLEMENT, etc.)")
    public ResponseEntity<List<FileMetadataResponse>> listByEntity(
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId) {
        return ResponseEntity.ok(storageService.listByEntity(entityType, entityId));
    }
}
