package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerminationQuizQuestionRequest {

    @NotNull(message = "El id del empleado es obligatorio")
    @Schema(description = "ID del empleado asociado al quiz", example = "42")
    private Long employeeId;

    @NotBlank(message = "La pregunta es obligatoria")
    @Schema(description = "Texto de la pregunta", example = "¿Cómo evaluaría la conducta del trabajador?")
    private String question;

    @Schema(description = "Grupo o sección de la pregunta", example = "Evaluación de Desempeño")
    private String questionGroup;

    @Schema(description = "Indica si la pregunta es obligatoria", example = "true")
    private Boolean required = true;

    @Schema(description = "Orden de visualización", example = "1")
    private Integer displayOrder;

    @Valid
    @NotEmpty(message = "La pregunta debe tener al menos una opción")
    @Schema(description = "Opciones de respuesta")
    private List<TerminationQuizOptionRequest> options;
}
