package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "health_insurance_tariffs")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class HealthInsuranceTariff {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
