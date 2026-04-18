package com.crm.common.repository;

import com.crm.common.entity.FileMetadata;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<FileMetadata> findByUploadedBy(Long uploadedBy);

    Optional<FileMetadata> findByS3Key(String s3Key);
}
