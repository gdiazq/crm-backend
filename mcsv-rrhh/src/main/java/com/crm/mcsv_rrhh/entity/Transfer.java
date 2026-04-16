package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers", indexes = {
        @Index(name = "idx_transfer_employee_id",  columnList = "employee_id"),
        @Index(name = "idx_transfer_effective_date", columnList = "effective_date"),
        @Index(name = "idx_transfer_created_at",   columnList = "created_at")
})
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class Transfer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @Column(name = "from_cost_center", nullable = false)
    private Integer fromCostCenter;

    @Column(name = "to_cost_center", nullable = false)
    private Integer toCostCenter;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(columnDefinition = "TEXT")
    private String reason;

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
