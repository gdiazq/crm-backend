package com.crm.mcsv_storage.service;

import com.crm.mcsv_storage.dto.FileMetadataResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {

    FileMetadataResponse upload(MultipartFile file, Long uploadedBy, String entityType, Long entityId);

    String getPresignedUrl(Long id);

    void delete(Long id, Long userId);

    List<FileMetadataResponse> listByEntity(String entityType, Long entityId);

    FileMetadataResponse getById(Long id);
}
