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
    private String personalEmail;
    private String phone;

    // Empresa
    private Long companyId;

    // Estado
    private Boolean active;
    private Long statusId;
    private Boolean rehireEligible;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Datos del User (mcsv-user via Feign — solo en detalle)
    private String username;
    private String userEmail;
    private Boolean userEnabled;
}
