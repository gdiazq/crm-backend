package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "communes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Commune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long regionId;
}
