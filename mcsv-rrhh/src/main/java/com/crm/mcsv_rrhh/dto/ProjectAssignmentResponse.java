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
public class ProjectAssignmentResponse {

    private Long id;
    private Long employeeId;
    private String employeeFullName;
    private String employeeIdentification;
    private Integer costCenter;
    private String projectName;
    private String roleOnProject;
    private BigDecimal allocationPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
