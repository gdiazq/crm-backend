package com.crm.mcsv_rrhh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceMarkRequest {

    @NotNull(message = "El empleado es obligatorio")
    private Long employeeId;

    private Long attendanceId;

    private Long projectAssignmentId;

    private Integer costCenter;

    private Long statusId;

    @NotNull(message = "La fecha/hora del marcaje es obligatoria")
    private LocalDateTime markTime;

    @NotBlank(message = "El tipo de marcaje es obligatorio")
    private String markType;

    private String notes;
}
