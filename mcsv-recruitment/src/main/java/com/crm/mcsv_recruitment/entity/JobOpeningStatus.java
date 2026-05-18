package com.crm.mcsv_recruitment.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_opening_statuses")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class JobOpeningStatus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
