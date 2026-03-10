package com.crm.mcsv_rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractDetailResponse {

    private Long id;
    private Long employeeId;

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
    private Boolean active;

    // ─── Auditoría ────────────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Solicitud RRHH ───────────────────────────────────────────────────────
    private Long requestId;
}
