package com.crm.mcsv_rrhh.entity.employee;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee_statuses")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class EmployeeStatus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
