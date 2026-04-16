package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransferRequest {

    @NotNull(message = "El ID del traspaso es obligatorio")
    @Schema(description = "ID del traspaso a actualizar")
    private Long id;

    @NotNull(message = "El centro de costo destino es obligatorio")
    @Schema(description = "Centro de costo destino")
    private Integer toCostCenter;

    @NotNull(message = "La fecha efectiva es obligatoria")
    @Schema(description = "Fecha efectiva del traspaso")
    private LocalDate effectiveDate;

    @Schema(description = "Motivo del traspaso")
    private String reason;
}
