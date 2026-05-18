package com.crm.mcsv_recruitment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_openings", indexes = {
        @Index(name = "idx_job_opening_status_id",            columnList = "status_id"),
        @Index(name = "idx_job_opening_cost_center",          columnList = "cost_center"),
        @Index(name = "idx_job_opening_supervisor_user_id",   columnList = "supervisor_user_id"),
        @Index(name = "idx_job_opening_close_date",           columnList = "close_date"),
        @Index(name = "idx_job_opening_created_at",           columnList = "created_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobOpening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "job_title_id", nullable = false)
    private Long jobTitleId;

    @Column(name = "cost_center", nullable = false)
    private Integer costCenter;

    @Column(nullable = false)
    private Integer headcount;

    @Column(name = "supervisor_user_id", nullable = false)
    private Long supervisorUserId;

    @Column(name = "reference_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal referenceSalary;

    @Column(name = "close_date", nullable = false)
    private LocalDate closeDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", insertable = false, updatable = false)
    private JobOpeningStatus status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
