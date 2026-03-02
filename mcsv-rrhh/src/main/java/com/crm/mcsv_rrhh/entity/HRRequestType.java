package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hr_request_types")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class HRRequestType {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean requireApproval = true;
}
