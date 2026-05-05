package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAssignmentRequest {

    @NotNull(message = "El empleado es obligatorio")
    private Long employeeId;

    @NotNull(message = "El centro de costo es obligatorio")
    private Integer costCenter;

    @Schema(description = "Rol del empleado en el proyecto/centro de costo")
    private String roleOnProject;

    @DecimalMin(value = "0.00", message = "La asignación no puede ser negativa")
    @DecimalMax(value = "100.00", message = "La asignación no puede ser mayor a 100")
    private BigDecimal allocationPercent;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean active;

    @Schema(description = "Permite regularizar una asignación vigente desde la ficha del empleado")
    private Boolean regularization;
}
