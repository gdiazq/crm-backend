package com.crm.mcsv_rrhh.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class UpdateContractRequest {

    @NotNull(message = "id es requerido")
    private Long id;

    @NotNull(message = "employeeId es requerido")
    private Long employeeId;

    // ─── Datos del contrato ───────────────────────────────────────────────────
    private String name;
    private String contractNumber;
    private Long contractTypeId;
    private Long safetyGroupId;
    private String contractDetail;
    private String baseSalary;
    private String agreedSalary;

    // ─── Datos organizacionales ───────────────────────────────────────────────
    private Long companyId;
    private Long zoneId;
    private Long jobTitleId;
    private Long siteId;
    private Long laborUnionId;

    // ─── Jornada laboral ──────────────────────────────────────────────────────
    private String weeklyWorkHours;
    private String workDays;

    // ─── Fechas ───────────────────────────────────────────────────────────────
    private LocalDate startDate;
    private LocalDate endDate;

    // ─── Beneficios ───────────────────────────────────────────────────────────
    private Long mealTypeId;
    private Long transportTypeId;
}
