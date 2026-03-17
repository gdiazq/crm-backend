package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafetyComplianceRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre de la categoría de cumplimiento de seguridad", example = "Cumplimiento Total")
    private String name;

    @Schema(description = "Descripción de la categoría")
    private String description;
}
