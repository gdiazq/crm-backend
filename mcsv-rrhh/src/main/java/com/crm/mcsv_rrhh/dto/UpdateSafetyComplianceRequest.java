package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSafetyComplianceRequest {

    @NotNull(message = "El id es obligatorio")
    @Schema(description = "ID de la categoría de cumplimiento de seguridad")
    private Long id;

    @Schema(description = "Nombre de la categoría de cumplimiento de seguridad")
    private String name;

    @Schema(description = "Descripción de la categoría")
    private String description;
}
