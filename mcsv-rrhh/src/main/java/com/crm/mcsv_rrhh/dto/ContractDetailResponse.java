package com.crm.mcsv_rrhh.dto;

import com.crm.common.dto.FileMetadataResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractDetailResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeIdentification;

    // ─── Datos del contrato ───────────────────────────────────────────────────
    private String name;
    private String contractNumber;
    private CatalogItem contractType;
    private CatalogItem contractStatus;
    private CatalogItem safetyGroup;
    private String contractDetail;
    private String baseSalary;
    private String agreedSalary;

    // ─── Datos organizacionales ───────────────────────────────────────────────
    private CatalogItem company;
    private CatalogItem zone;
    private CatalogItem jobTitle;
    private CatalogItem site;
    private CatalogItem laborUnion;

    // ─── Jornada laboral ──────────────────────────────────────────────────────
    private String weeklyWorkHours;
    private String workDays;

    // ─── Fechas ───────────────────────────────────────────────────────────────
    private LocalDate startDate;
    private LocalDate endDate;

    // ─── Beneficios ───────────────────────────────────────────────────────────
    private CatalogItem mealType;
    private CatalogItem transportType;

    // ─── Estado ───────────────────────────────────────────────────────────────
    private CatalogItem status;

    // ─── Auditoría ────────────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Solicitud RRHH ───────────────────────────────────────────────────────
    private Long requestId;

    // ─── Documentos adjuntos ──────────────────────────────────────────────────
    private List<FileMetadataResponse> documents;
}
