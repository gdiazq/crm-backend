package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paid = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean requiresDocument = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean requireApproval = false;

    @Column
    private Integer maxDaysPerYear;
}
