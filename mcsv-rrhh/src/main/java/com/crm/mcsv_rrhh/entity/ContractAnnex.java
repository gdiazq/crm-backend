package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_annexes", indexes = {
        @Index(name = "idx_contract_annex_employee_id", columnList = "employee_id"),
        @Index(name = "idx_contract_annex_contract_id", columnList = "contract_id"),
        @Index(name = "idx_contract_annex_created_at",  columnList = "created_at")
})
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ContractAnnex {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "annex_type_id", nullable = false)
    private Long annexTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annex_type_id", insertable = false, updatable = false)
    private ContractAnnexType annexType;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Formula("(SELECT es.name FROM hr_requests hr JOIN employee_statuses es ON es.id = hr.status_id WHERE hr.annex_id = id ORDER BY hr.created_at DESC LIMIT 1)")
    private String currentStatusName;

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
