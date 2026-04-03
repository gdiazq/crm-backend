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
public class SettlementRequest {

    @NotNull(message = "El empleado es obligatorio")
    @Schema(description = "ID del empleado")
    private Long employeeId;

    @NotNull(message = "El contrato es obligatorio")
    @Schema(description = "ID del contrato")
    private Long contractId;

    @NotNull(message = "La fecha de término es obligatoria")
    @Schema(description = "Fecha de término del contrato")
    private LocalDate endDate;

    @Schema(description = "ID de la causal legal de término")
    private Long legalTerminationCauseId;

    @Schema(description = "ID de la evaluación de calidad de trabajo")
    private Long qualityOfWorkId;

    @Schema(description = "ID del cumplimiento de seguridad")
    private Long safetyComplianceId;

    @Schema(description = "Indica si el empleado es elegible para recontratación", example = "true")
    private Boolean rehireEligible = true;

    @Schema(description = "ID de la causa de no recontratación (solo si rehireEligible=false)")
    private Long noReHiredCauseId;

    @Schema(description = "Observaciones adicionales")
    private String observations;

    @Schema(description = "ID de la solicitud HR relacionada (opcional)")
    private Long hrRequestId;
}
