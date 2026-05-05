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
public class AttendanceRequest {

    @NotNull(message = "El empleado es obligatorio")
    private Long employeeId;

    private Long projectAssignmentId;

    private Integer costCenter;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    @NotNull(message = "El estado de asistencia es obligatorio")
    private Long statusId;

    private String notes;
}
