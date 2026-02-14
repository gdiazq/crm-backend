package com.crm.mcsv_storage.service.impl;

import com.crm.mcsv_storage.dto.FileMetadataResponse;
import com.crm.mcsv_storage.entity.FileMetadata;
import com.crm.mcsv_storage.exception.StorageException;
import com.crm.mcsv_storage.repository.FileMetadataRepository;
import com.crm.mcsv_storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    @Override
    @Transactional
    public FileMetadataResponse upload(MultipartFile file, Long uploadedBy, String entityType, Long entityId, Boolean isPublic) {
        if (file.isEmpty()) {
            throw new StorageException("Cannot upload empty file");
        }

        boolean publicFile = Boolean.TRUE.equals(isPublic);
        String originalFileName = file.getOriginalFilename();
        String s3Key = buildS3Key(entityType, entityId, originalFileName);

        try {
            PutObjectRequest.Builder putBuilder = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(file.getContentType());

            if (publicFile) {
                putBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            }

            s3Client.putObject(putBuilder.build(), RequestBody.fromBytes(file.getBytes()));
            log.info("File uploaded to S3: {} (public={})", s3Key, publicFile);
        } catch (IOException e) {
            throw new StorageException("Failed to read file content", e);
        } catch (Exception e) {
            throw new StorageException("Failed to upload file to S3: " + e.getMessage(), e);
        }

        FileMetadata metadata = FileMetadata.builder()
                .fileName(originalFileName)
                .s3Key(s3Key)
                .bucket(bucket)
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploadedBy(uploadedBy)
                .entityType(entityType)
                .entityId(entityId)
                .isPublic(publicFile)
                .build();

        FileMetadata saved = fileMetadataRepository.save(metadata);
        log.info("File metadata saved with id: {}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public String getPresignedUrl(Long id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new StorageException("File not found with id: " + id));

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(metadata.getBucket())
                .key(metadata.getS3Key())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new StorageException("File not found with id: " + id));

        if (!metadata.getUploadedBy().equals(userId)) {
            throw new StorageException("User does not have permission to delete this file");
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(metadata.getBucket())
                    .key(metadata.getS3Key())
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted from S3: {}", metadata.getS3Key());
        } catch (Exception e) {
            throw new StorageException("Failed to delete file from S3: " + e.getMessage(), e);
        }

        fileMetadataRepository.delete(metadata);
        log.info("File metadata deleted with id: {}", id);
    }

    @Override
    public List<FileMetadataResponse> listByEntity(String entityType, Long entityId) {
        return fileMetadataRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public FileMetadataResponse getById(Long id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new StorageException("File not found with id: " + id));
        return toResponse(metadata);
    }

    private String buildS3Key(String entityType, Long entityId, String fileName) {
        String safeEntityType = entityType != null ? entityType : "general";
        String safeEntityId = entityId != null ? entityId.toString() : "0";
        return safeEntityType + "/" + safeEntityId + "/" + UUID.randomUUID() + "_" + fileName;
    }

    private FileMetadataResponse toResponse(FileMetadata metadata) {
        String url = null;
        if (Boolean.TRUE.equals(metadata.getIsPublic())) {
            url = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    metadata.getBucket(), region, metadata.getS3Key());
        }

        return FileMetadataResponse.builder()
                .id(metadata.getId())
                .fileName(metadata.getFileName())
                .contentType(metadata.getContentType())
                .size(metadata.getSize())
                .url(url)
                .entityType(metadata.getEntityType())
                .entityId(metadata.getEntityId())
                .createdAt(metadata.getCreatedAt())
                .build();
    }
}
