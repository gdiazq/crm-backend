package com.crm.mcsv_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProjectSpecialtyRequest {

    @NotNull(message = "El id es obligatorio")
    @Schema(description = "ID de la especialidad")
    private Long id;

    @Schema(description = "Nombre de la especialidad")
    private String name;

    @Schema(description = "Descripción de la especialidad")
    private String description;
}
