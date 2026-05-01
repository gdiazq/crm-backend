package com.crm.mcsv_rrhh.dto;

import com.crm.common.dto.FileMetadataResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLeaveResponse {

    private Long id;
    private String status;
    private Long employeeId;
    private String employeeFullName;
    private String employeeIdentification;
    private Long contractId;
    private Long leaveTypeId;
    private String leaveTypeName;
    private Boolean paid;
    private Boolean requiresDocument;
    private Boolean requireApproval;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean halfDay;
    private BigDecimal totalDays;
    private String reason;
    private List<FileMetadataResponse> documents;
    private Long hrRequestId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
