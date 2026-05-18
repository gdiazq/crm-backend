package com.crm.mcsv_recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobOpeningRequest {

    @NotBlank
    @Size(max = 200)
    @Schema(description = "Título de la vacante", example = "Desarrollador Backend Senior")
    private String title;

    @NotNull
    @Schema(description = "ID del cargo (catálogo job_titles de mcsv-rrhh)")
    private Long jobTitleId;

    @NotNull
    @Schema(description = "Centro de costo / proyecto donde se contratará")
    private Integer costCenter;

    @NotNull
    @Min(1)
    @Schema(description = "Cantidad de cupos a contratar")
    private Integer headcount;

    @NotNull
    @Schema(description = "ID del usuario supervisor")
    private Long supervisorUserId;

    @NotNull
    @DecimalMin("0.0")
    @Schema(description = "Sueldo bruto de referencia")
    private BigDecimal referenceSalary;

    @NotNull
    @Future
    @Schema(description = "Fecha estimada de cierre del proceso")
    private LocalDate closeDate;

    @Schema(description = "Descripción de la vacante")
    private String description;

    @Schema(description = "Requisitos del puesto")
    private String requirements;
}
