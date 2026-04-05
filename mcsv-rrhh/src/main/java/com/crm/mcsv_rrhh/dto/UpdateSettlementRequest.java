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
public class UpdateSettlementRequest {

    @NotNull(message = "El id es obligatorio")
    @Schema(description = "ID del finiquito")
    private Long id;

    @Schema(description = "Fecha de término del contrato")
    private LocalDate endDate;

    @Schema(description = "ID de la causal legal de término")
    private Long legalTerminationCauseId;

    @Schema(description = "ID de la evaluación de calidad de trabajo")
    private Long qualityOfWorkId;

    @Schema(description = "ID del cumplimiento de seguridad")
    private Long safetyComplianceId;

    @Schema(description = "Indica si el empleado es elegible para recontratación")
    private Boolean rehireEligible;

    @Schema(description = "ID de la causa de no recontratación (solo si rehireEligible=false)")
    private Long noReHiredCauseId;

    @Schema(description = "Observaciones adicionales")
    private String observations;
}
