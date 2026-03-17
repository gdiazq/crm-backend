package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalTerminationCauseRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre de la causal legal de término", example = "Artículo 159 N°1 - Mutuo acuerdo")
    private String name;

    @Schema(description = "Descripción detallada de la causal")
    private String description;
}
