package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hr_requests", indexes = {
        @Index(name = "idx_hr_request_id_module",   columnList = "id_module"),
        @Index(name = "idx_hr_request_status_id",   columnList = "status_id"),
        @Index(name = "idx_hr_request_type_id",     columnList = "request_type_id"),
        @Index(name = "idx_hr_request_contract_id", columnList = "contract_id"),
        @Index(name = "idx_hr_request_created_at",  columnList = "created_at"),
        @Index(name = "idx_hr_request_module_status", columnList = "id_module, status_id")
})
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class HRRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requestTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_module", insertable = false, updatable = false)
    private Employee employee;

    @Column(nullable = false)
    private Long statusId;

    @Column(nullable = false)
    private Boolean requireApproval;

    @Column(name = "id_module", nullable = false)
    private Long idModule;

    @Column
    private Long approverId;

    @Column
    private LocalDateTime approvalDate;

    @Column
    private Long hhrrApproverId;

    @Column
    private LocalDateTime hhrrApprovalDate;

    @Column
    private Long contractId;

    @Column
    private String action;

    @Column(columnDefinition = "TEXT")
    private String proposedData;

    @Column
    private String rejectionDetail;

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
