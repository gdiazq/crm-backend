package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts", indexes = {
        @Index(name = "idx_contract_employee_id",       columnList = "employee_id"),
        @Index(name = "idx_contract_status_id",         columnList = "status_id"),
        @Index(name = "idx_contract_contract_status_id",columnList = "contract_status_id"),
        @Index(name = "idx_contract_company_id",        columnList = "company_id"),
        @Index(name = "idx_contract_start_date",        columnList = "start_date"),
        @Index(name = "idx_contract_created_at",        columnList = "created_at"),
        @Index(name = "idx_contract_employee_status",   columnList = "employee_id, contract_status_id")
})
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class Contract {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

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
