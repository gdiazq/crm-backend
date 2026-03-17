package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLegalTerminationCauseRequest {

    @NotNull(message = "El id es obligatorio")
    @Schema(description = "ID de la causal legal de término")
    private Long id;

    @Schema(description = "Nombre de la causal legal de término")
    private String name;

    @Schema(description = "Descripción detallada de la causal")
    private String description;
}
