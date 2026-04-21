package com.crm.mcsv_rrhh.dto;

import com.crm.common.dto.FileMetadataResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractAnnexResponse {

    private Long id;
    private String status;
    private Long employeeId;
    private String employeeFullName;
    private String employeeIdentification;
    private Long contractId;
    private Long annexTypeId;
    private String annexTypeName;
    private Boolean requireApproval;
    private LocalDate date;
    private String description;
    private List<FileMetadataResponse> documents;
    private Long hrRequestId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
