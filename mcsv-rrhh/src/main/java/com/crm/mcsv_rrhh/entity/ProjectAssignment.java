package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_assignments", indexes = {
        @Index(name = "idx_pa_employee", columnList = "employee_id"),
        @Index(name = "idx_pa_cost_center", columnList = "cost_center"),
        @Index(name = "idx_pa_employee_active", columnList = "employee_id, active"),
        @Index(name = "idx_pa_cost_center_active", columnList = "cost_center, active")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @Column(name = "cost_center", nullable = false)
    private Integer costCenter;

    @Column(name = "role_on_project", length = 120)
    private String roleOnProject;

    @Column(name = "allocation_percent", precision = 5, scale = 2)
    private BigDecimal allocationPercent;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
