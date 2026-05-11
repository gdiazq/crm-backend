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
public class OvertimeResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long contractId;
    private Integer costCenter;
    private String projectName;
    private Long overtimeTypeId;
    private String overtimeTypeName;
    private BigDecimal surchargePercent;
    private Long attendanceId;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal hours;
    private String reason;
    private String currentStatusName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
