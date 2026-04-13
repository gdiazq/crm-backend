package com.crm.common.storage.service;

import com.crm.common.dto.FileMetadataResponse;
import com.crm.common.storage.entity.FileMetadata;
import com.crm.common.storage.exception.StorageException;
import com.crm.common.storage.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class StorageServiceImpl implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Override
    @Transactional
    public FileMetadataResponse upload(MultipartFile file, Long uploadedBy, String entityType, Long entityId, Boolean isPublic) {
        if (file.isEmpty()) throw new StorageException("Cannot upload empty file");

        boolean publicFile = Boolean.TRUE.equals(isPublic);
        String s3Key = buildS3Key(entityType, entityId, file.getOriginalFilename());

        try {
            PutObjectRequest.Builder putBuilder = PutObjectRequest.builder()
                    .bucket(bucket).key(s3Key).contentType(file.getContentType());
            if (publicFile) putBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            s3Client.putObject(putBuilder.build(), RequestBody.fromBytes(file.getBytes()));
            log.info("File uploaded to S3: {}", s3Key);
        } catch (IOException e) {
            throw new StorageException("Failed to read file content", e);
        } catch (Exception e) {
            throw new StorageException("Failed to upload file to S3: " + e.getMessage(), e);
        }

        FileMetadata saved = fileMetadataRepository.save(FileMetadata.builder()
                .fileName(file.getOriginalFilename()).s3Key(s3Key).bucket(bucket)
                .contentType(file.getContentType()).size(file.getSize())
                .uploadedBy(uploadedBy).entityType(entityType).entityId(entityId)
                .isPublic(publicFile).build());

        return toResponse(saved);
    }

    @Override
    public String getPresignedUrl(Long id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new StorageException("File not found: " + id));

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(metadata.getBucket()).key(metadata.getS3Key()).build())
                        .build());
        return presigned.url().toString();
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new StorageException("File not found: " + id));
        if (!metadata.getUploadedBy().equals(userId))
            throw new StorageException("User does not have permission to delete this file");

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(metadata.getBucket()).key(metadata.getS3Key()).build());
        } catch (Exception e) {
            throw new StorageException("Failed to delete from S3: " + e.getMessage(), e);
        }
        fileMetadataRepository.delete(metadata);
    }

    @Override
    public List<FileMetadataResponse> listByEntity(String entityType, Long entityId) {
        return fileMetadataRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public FileMetadataResponse getById(Long id) {
        return toResponse(fileMetadataRepository.findById(id)
                .orElseThrow(() -> new StorageException("File not found: " + id)));
    }

    @Override
    @Transactional
    public void retag(Long id, String newEntityType, Long newEntityId) {
        FileMetadata m = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new StorageException("File not found: " + id));
        m.setEntityType(newEntityType);
        m.setEntityId(newEntityId);
        fileMetadataRepository.save(m);
    }

    private String buildS3Key(String entityType, Long entityId, String fileName) {
        return (entityType != null ? entityType : "general") + "/" +
               (entityId != null ? entityId : "0") + "/" +
               UUID.randomUUID() + "_" + fileName;
    }

    private FileMetadataResponse toResponse(FileMetadata m) {
        String url = Boolean.TRUE.equals(m.getIsPublic())
                ? String.format("https://%s.s3.%s.amazonaws.com/%s", m.getBucket(), region, m.getS3Key())
                : null;
        return FileMetadataResponse.builder()
                .id(m.getId()).fileName(m.getFileName()).contentType(m.getContentType())
                .size(m.getSize()).url(url).entityType(m.getEntityType())
                .entityId(m.getEntityId()).createdAt(m.getCreatedAt()).build();
    }
}
