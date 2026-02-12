package com.crm.mcsv_storage.repository;

import com.crm.mcsv_storage.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<FileMetadata> findByUploadedBy(Long uploadedBy);

    Optional<FileMetadata> findByS3Key(String s3Key);
}
