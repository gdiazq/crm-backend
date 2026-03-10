package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class Contract {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    // ─── Datos del contrato ───────────────────────────────────────────────────
    private String name;
    private String contractNumber;
    private Long contractTypeId;
    private Long contractStatusId;
    private Long safetyGroupId;

    @Column(columnDefinition = "TEXT")
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

    // ─── Estado ───────────────────────────────────────────────────────────────
    private Long statusId;
    private Boolean active;

    // ─── Auditoría ────────────────────────────────────────────────────────────
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
