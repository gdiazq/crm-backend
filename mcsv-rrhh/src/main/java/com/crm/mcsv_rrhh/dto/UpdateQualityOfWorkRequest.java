package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQualityOfWorkRequest {

    @NotNull(message = "El id es obligatorio")
    @Schema(description = "ID de la evaluación de calidad de trabajo")
    private Long id;

    @Schema(description = "Nombre de la evaluación de calidad de trabajo")
    private String name;

    @Schema(description = "Descripción de la evaluación")
    private String description;
}
