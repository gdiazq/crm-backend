package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendances", indexes = {
        @Index(name = "idx_attendance_employee_id", columnList = "employee_id"),
        @Index(name = "idx_attendance_date", columnList = "date"),
        @Index(name = "idx_attendance_cost_center", columnList = "cost_center"),
        @Index(name = "idx_attendance_generated_by_leave", columnList = "generated_by_leave_id"),
        @Index(name = "idx_attendance_created_at", columnList = "created_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_attendance_emp_date", columnNames = {"employee_id", "date"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "project_assignment_id")
    private Long projectAssignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_assignment_id", insertable = false, updatable = false)
    private ProjectAssignment projectAssignment;

    @Column(name = "cost_center")
    private Integer costCenter;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "total_hours", precision = 5, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", insertable = false, updatable = false)
    private AttendanceStatus status;

    @Column(name = "generated_by_leave_id")
    private Long generatedByLeaveId;

    @Column(name = "manually_overridden", nullable = false)
    @Builder.Default
    private Boolean manuallyOverridden = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Formula("(EXISTS (SELECT 1 FROM employee_leaves el WHERE el.employee_id = employee_id AND date BETWEEN el.start_date AND el.end_date AND (SELECT es.name FROM hr_requests hr JOIN employee_statuses es ON es.id = hr.status_id WHERE hr.leave_id = el.id ORDER BY hr.created_at DESC LIMIT 1) = 'Aprobado'))")
    private Boolean hasActiveLeave;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (manuallyOverridden == null) manuallyOverridden = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
