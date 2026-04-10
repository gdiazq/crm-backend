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
public class SettlementResponse {

    private Long id;
    private String status;
    private Long employeeId;
    private String employeeFullName;
    private String employeeIdentification;
    private Long contractId;
    private LocalDate endDate;
    private Long legalTerminationCauseId;
    private String legalTerminationCauseName;
    private Long qualityOfWorkId;
    private String qualityOfWorkName;
    private Long safetyComplianceId;
    private String safetyComplianceName;
    private Boolean rehireEligible;
    private Long noReHiredCauseId;
    private String noReHiredCauseName;
    private List<FileMetadataResponse> documents;
    private String observations;
    private Long hrRequestId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
