package com.crm.mcsv_rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminationAgreementResponse {

    private Long id;
    private String status;

    // Empleado
    private Long employeeId;
    private String employeeFullName;
    private String employeeIdentification;

    // Contrato
    private Long contractId;

    // Fecha término
    private LocalDate endDate;

    // Catálogos
    private Long legalTerminationCauseId;
    private String legalTerminationCauseName;

    private Long qualityOfWorkId;
    private String qualityOfWorkName;

    private Long safetyComplianceId;
    private String safetyComplianceName;

    // Recontratación
    private Boolean rehireEligible;
    private Long noReHiredCauseId;
    private String noReHiredCauseName;

    // Documento
    private String terminationDocumentUrl;

    // Observaciones
    private String observations;

    // HR Request
    private Long hrRequestId;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
