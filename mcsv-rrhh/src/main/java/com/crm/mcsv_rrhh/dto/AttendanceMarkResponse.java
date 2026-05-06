package com.crm.mcsv_rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceMarkResponse {
    private Long id;
    private Long attendanceId;
    private Long employeeId;
    private String employeeFullName;
    private String employeeIdentification;
    private Long projectAssignmentId;
    private Integer costCenter;
    private String projectName;
    private LocalDate date;
    private LocalDateTime markTime;
    private String markType;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
