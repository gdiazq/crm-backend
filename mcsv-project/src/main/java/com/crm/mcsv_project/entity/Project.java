package com.crm.mcsv_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects", indexes = {
        @Index(name = "idx_project_active",       columnList = "active"),
        @Index(name = "idx_project_cost_center",  columnList = "cost_center"),
        @Index(name = "idx_project_created_at",   columnList = "created_at"),
        @Index(name = "idx_project_type_id",      columnList = "type_id"),
        @Index(name = "idx_project_status_id",    columnList = "status_id"),
        @Index(name = "idx_project_specialty_id", columnList = "specialty_id")
})
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class Project {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cost_center", nullable = false, unique = true)
    private Integer costCenter;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private ProjectType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private ProjectStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id")
    private ProjectSpecialty specialty;

    @Column(name = "visitor_id")
    private Long visitorId;

    @Column(name = "supervisor_id")
    private Long supervisorId;

    @ElementCollection
    @CollectionTable(name = "project_company_representatives", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "employee_id")
    @Builder.Default
    private List<Long> companyRepresentativeIds = new ArrayList<>();

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "real_start_date")
    private LocalDate realStartDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "real_end_date")
    private LocalDate realEndDate;

    @Builder.Default
    private Boolean active = true;

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
