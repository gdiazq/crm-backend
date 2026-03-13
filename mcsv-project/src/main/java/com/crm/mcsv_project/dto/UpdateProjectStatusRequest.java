package com.crm.mcsv_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProjectStatusRequest {

    @NotNull(message = "El id es obligatorio")
    @Schema(description = "ID del estado de proyecto")
    private Long id;

    @Schema(description = "Nombre del estado de proyecto")
    private String name;

    @Schema(description = "Descripción del estado de proyecto")
    private String description;
}
