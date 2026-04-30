package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_leaves", indexes = {
        @Index(name = "idx_employee_leave_employee_id", columnList = "employee_id"),
        @Index(name = "idx_employee_leave_contract_id", columnList = "contract_id"),
        @Index(name = "idx_employee_leave_leave_type_id", columnList = "leave_type_id"),
        @Index(name = "idx_employee_leave_start_date", columnList = "start_date"),
        @Index(name = "idx_employee_leave_created_at", columnList = "created_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "leave_type_id", nullable = false)
    private Long leaveTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", insertable = false, updatable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean halfDay = false;

    @Column(nullable = false, precision = 10, scale = 1)
    private BigDecimal totalDays;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Formula("(SELECT es.name FROM hr_requests hr JOIN employee_statuses es ON es.id = hr.status_id WHERE hr.leave_id = id ORDER BY hr.created_at DESC LIMIT 1)")
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
