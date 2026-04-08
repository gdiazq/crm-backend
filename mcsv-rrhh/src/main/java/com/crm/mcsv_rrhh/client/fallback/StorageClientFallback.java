package com.crm.mcsv_rrhh.client.fallback;

import com.crm.mcsv_rrhh.client.StorageClient;
import com.crm.mcsv_rrhh.dto.FileMetadataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StorageClientFallback implements StorageClient {

    @Override
    public ResponseEntity<FileMetadataResponse> upload(MultipartFile file, Long uploadedBy,
                                                        String entityType, Long entityId, Boolean isPublic) {
        log.error("mcsv-storage no disponible — upload fallido para entityType={} entityId={}", entityType, entityId);
        throw new RuntimeException("Servicio de almacenamiento no disponible. Intente más tarde.");
    }

    @Override
    public ResponseEntity<Void> delete(Long id, Long userId) {
        log.error("mcsv-storage no disponible — delete fallido para id={}", id);
        throw new RuntimeException("Servicio de almacenamiento no disponible. Intente más tarde.");
    }

    @Override
    public ResponseEntity<List<FileMetadataResponse>> listByEntity(String entityType, Long entityId) {
        log.warn("mcsv-storage no disponible — retornando lista vacía para entityType={} entityId={}", entityType, entityId);
        return ResponseEntity.ok(Collections.emptyList());
    }

    @Override
    public ResponseEntity<Void> retag(Long id, String entityType, Long entityId) {
        log.error("mcsv-storage no disponible — retag fallido para id={}", id);
        throw new RuntimeException("Servicio de almacenamiento no disponible. Intente más tarde.");
    }
}
