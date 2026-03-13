package com.crm.mcsv_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectSpecialtyRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre de la especialidad", example = "Ingeniería Civil")
    private String name;

    @Schema(description = "Descripción de la especialidad")
    private String description;

    @Schema(description = "Estado activo", example = "true")
    private Boolean active = true;
}
