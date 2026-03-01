package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_allowance_tiers")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class FamilyAllowanceTier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
