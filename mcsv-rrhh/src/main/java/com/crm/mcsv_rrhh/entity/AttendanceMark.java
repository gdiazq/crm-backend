package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_marks", indexes = {
        @Index(name = "idx_att_mark_attendance", columnList = "attendance_id"),
        @Index(name = "idx_att_mark_employee", columnList = "employee_id"),
        @Index(name = "idx_att_mark_cost_center", columnList = "cost_center"),
        @Index(name = "idx_att_mark_date", columnList = "date"),
        @Index(name = "idx_att_mark_type", columnList = "mark_type")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_att_mark_att_type", columnNames = {"attendance_id", "mark_type"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attendance_id", nullable = false)
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", insertable = false, updatable = false)
    private Attendance attendance;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @Column(name = "project_assignment_id")
    private Long projectAssignmentId;

    @Column(name = "cost_center")
    private Integer costCenter;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "mark_time", nullable = false)
    private LocalDateTime markTime;

    @Column(name = "mark_type", nullable = false, length = 20)
    private String markType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

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
