package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "overtimes", indexes = {
        @Index(name = "idx_overtime_employee_id", columnList = "employee_id"),
        @Index(name = "idx_overtime_attendance_id", columnList = "attendance_id"),
        @Index(name = "idx_overtime_cost_center", columnList = "cost_center"),
        @Index(name = "idx_overtime_date", columnList = "date"),
        @Index(name = "idx_overtime_created_at", columnList = "created_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Overtime {

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

    @Column(name = "cost_center", nullable = false)
    private Integer costCenter;

    @Column(name = "overtime_type_id", nullable = false)
    private Long overtimeTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overtime_type_id", insertable = false, updatable = false)
    private OvertimeType overtimeType;

    @Column(name = "attendance_id", nullable = false)
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", insertable = false, updatable = false)
    private Attendance attendance;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Formula("(SELECT es.name FROM hr_requests hr JOIN employee_statuses es ON es.id = hr.status_id WHERE hr.overtime_id = id ORDER BY hr.created_at DESC LIMIT 1)")
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
