package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoReHiredCauseRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre de la causa de no recontratación", example = "Incumplimiento grave de normas")
    private String name;

    @Schema(description = "Descripción de la causa")
    private String description;
}
