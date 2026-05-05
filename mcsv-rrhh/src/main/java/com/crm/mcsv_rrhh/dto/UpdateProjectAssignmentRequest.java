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
public class UpdateProjectAssignmentRequest {

    @NotNull(message = "El ID de la asignación es obligatorio")
    private Long id;

    private Long employeeId;

    private Integer costCenter;

    @Schema(description = "Rol del empleado en el proyecto/centro de costo")
    private String roleOnProject;

    @DecimalMin(value = "0.00", message = "La asignación no puede ser negativa")
    @DecimalMax(value = "100.00", message = "La asignación no puede ser mayor a 100")
    private BigDecimal allocationPercent;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean active;
}
