package com.crm.mcsv_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectStatusRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre del estado de proyecto", example = "En progreso")
    private String name;

    @Schema(description = "Descripción del estado de proyecto")
    private String description;

    @Schema(description = "Estado activo", example = "true")
    private Boolean active = true;
}
