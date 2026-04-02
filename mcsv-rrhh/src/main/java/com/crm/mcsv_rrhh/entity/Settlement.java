package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements", indexes = {
        @Index(name = "idx_stl_employee_id",  columnList = "employee_id"),
        @Index(name = "idx_stl_contract_id",  columnList = "contract_id"),
        @Index(name = "idx_stl_status",       columnList = "status"),
        @Index(name = "idx_stl_end_date",     columnList = "end_date"),
        @Index(name = "idx_stl_created_at",   columnList = "created_at")
})
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class Settlement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    private Contract contract;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_termination_cause_id")
    private LegalTerminationCause legalTerminationCause;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quality_of_work_id")
    private QualityOfWork qualityOfWork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "safety_compliance_id")
    private SafetyCompliance safetyCompliance;

    @Builder.Default
    @Column(nullable = false)
    private Boolean rehireEligible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "no_re_hired_cause_id")
    private NoReHiredCause noReHiredCause;

    private String terminationDocumentUrl;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(name = "hr_request_id")
    private Long hrRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_request_id", insertable = false, updatable = false)
    private HRRequest hrRequest;

    @Builder.Default
    @Column(nullable = false)
    private String status = "BORRADOR";

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
