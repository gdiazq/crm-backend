package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeUpdateRequest {

    @NotNull(message = "El id es obligatorio")
    @Schema(description = "ID del registro de hora extra a editar")
    private Long id;

    @NotNull(message = "El tipo de hora extra es obligatorio")
    @Schema(description = "ID del tipo de hora extra")
    private Long overtimeTypeId;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Schema(description = "Inicio del bloque (mismo día del registro)")
    private LocalDateTime startTime;

    @NotNull(message = "La hora de término es obligatoria")
    @Schema(description = "Término del bloque (mismo día del registro)")
    private LocalDateTime endTime;

    @Schema(description = "Motivo del registro")
    private String reason;
}
