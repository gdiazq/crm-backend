package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerminationQuizOptionRequest {

    @NotBlank(message = "El label de la opción es obligatorio")
    @Schema(description = "Texto de la opción", example = "Siempre")
    private String label;

    @Schema(description = "Orden de visualización", example = "1")
    private Integer displayOrder;
}
