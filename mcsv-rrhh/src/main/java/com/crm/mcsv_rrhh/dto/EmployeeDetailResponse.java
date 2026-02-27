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
public class EmployeeDetailResponse {

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

    // Estado
    private Boolean active;
    private Boolean rehireEligible;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Datos del User vinculado (mcsv-user via Feign)
    private String username;
    private String userEmail;
    private Boolean userEnabled;
}
