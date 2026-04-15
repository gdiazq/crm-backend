package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTerminationQuizQuestionRequest {

    @NotNull(message = "El id es obligatorio")
    @Schema(description = "ID de la pregunta")
    private Long id;

    @Schema(description = "ID del empleado asociado al quiz", example = "42")
    private Long employeeId;

    @Schema(description = "Texto de la pregunta")
    private String question;

    @Schema(description = "Grupo o sección de la pregunta", example = "Ambiente Laboral")
    private String questionGroup;

    @Schema(description = "Indica si la pregunta es obligatoria")
    private Boolean required;
}
