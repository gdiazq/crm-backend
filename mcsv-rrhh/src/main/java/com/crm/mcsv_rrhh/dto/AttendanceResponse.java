package com.crm.mcsv_rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;
    private Long employeeId;
    private String employeeFullName;
    private String employeeIdentification;
    private Long contractId;
    private Long projectAssignmentId;
    private Integer costCenter;
    private String projectName;
    private LocalDate date;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalHours;
    private Long statusId;
    private String statusName;
    private String statusCode;
    private Long generatedByLeaveId;
    private Boolean manuallyOverridden;
    private Boolean hasActiveLeave;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
