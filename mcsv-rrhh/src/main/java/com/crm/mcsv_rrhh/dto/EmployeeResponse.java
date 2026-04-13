package com.crm.mcsv_rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponse {

    private Long id;
    private Long userId;

    // Datos de identificación
    private String identification;

    // Nombre completo
    private String firstName;
    private String paternalLastName;
    private String maternalLastName;

    // Contacto
    private String corporateEmail;
    private String phone;

    // Estado RRHH
    private String statusName;

    // Proyecto asignado
    private Integer costCenter;
    private String projectName;

    // Estado
    private Boolean active;
    private Boolean rehireEligible;
    private Boolean hasContract;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
