package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "termination_quiz_question", indexes = {
        @Index(name = "idx_tqq_employee_id",  columnList = "employee_id"),
        @Index(name = "idx_tqq_active",       columnList = "active"),
        @Index(name = "idx_tqq_group",        columnList = "question_group"),
        @Index(name = "idx_tqq_created_at",   columnList = "created_at")
})
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class TerminationQuizQuestion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "question_group")
    private String questionGroup;

    @Builder.Default
    @Column(nullable = false)
    private Boolean required = true;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<TerminationQuizOption> options = new ArrayList<>();

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
