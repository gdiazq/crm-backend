package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hr_requests")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class HRRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requestTypeId;

    @Column(nullable = false)
    private Long statusId;

    @Column(nullable = false)
    private Boolean requireApproval;

    @Column(nullable = false)
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
