package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNoReHiredCauseRequest {

    @NotNull(message = "El id es obligatorio")
    @Schema(description = "ID de la causa de no recontratación")
    private Long id;

    @Schema(description = "Nombre de la causa de no recontratación")
    private String name;

    @Schema(description = "Descripción de la causa")
    private String description;
}
