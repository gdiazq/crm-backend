package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_question_group")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class QuizQuestionGroup {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Builder.Default
    private Boolean active = true;
}
