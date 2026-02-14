package com.crm.mcsv_user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataResponse {

    private Long id;
    private String fileName;
    private String contentType;
    private Long size;
    private String url;
    private String entityType;
    private Long entityId;
    private LocalDateTime createdAt;
}
