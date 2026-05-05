package com.crm.mcsv_rrhh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttendanceRequest {

    @NotNull(message = "El ID de asistencia es obligatorio")
    private Long id;

    private Long employeeId;

    private Long projectAssignmentId;

    private Integer costCenter;

    private LocalDate date;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private Long statusId;

    private String notes;
}
