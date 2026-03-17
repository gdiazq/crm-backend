package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityOfWorkRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre de la evaluación de calidad de trabajo", example = "Excelente")
    private String name;

    @Schema(description = "Descripción de la evaluación")
    private String description;
}
