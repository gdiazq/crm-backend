package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "identification_types")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdentificationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Builder.Default
    private Boolean status = true;
}
