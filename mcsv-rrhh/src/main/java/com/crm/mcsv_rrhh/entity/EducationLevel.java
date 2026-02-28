package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "education_levels")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EducationLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
