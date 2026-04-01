package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "termination_quiz_option", indexes = {
        @Index(name = "idx_tqo_question_id", columnList = "question_id")
})
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class TerminationQuizOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private TerminationQuizQuestion question;

    @Column(nullable = false)
    private String label;

    @Column(name = "display_order")
    private Integer displayOrder;
}
