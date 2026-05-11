package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "overtime_types")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OvertimeType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "surcharge_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal surchargePercent;

    @Column(name = "night_shift", nullable = false)
    @Builder.Default
    private Boolean nightShift = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean holiday = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
