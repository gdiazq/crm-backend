package com.crm.mcsv_rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDetailResponse {

    private Long id;
    private Long userId;

    // ─── Datos Personales ─────────────────────────────────────────────────────
    private String identification;
    private CatalogItem identificationType;

    private String firstName;
    private String paternalLastName;
    private String maternalLastName;

    private LocalDate birthDate;

    private CatalogItem gender;
    private CatalogItem maritalStatus;
    private CatalogItem educationLevel;
    private CatalogItem driverLicense;
    private CatalogItem profession;

    // ─── Datos de Contacto ────────────────────────────────────────────────────
    private String personalEmail;
    private String corporateEmail;
    private String phone;
    private String phone2;

    // ─── Contacto de Emergencia ───────────────────────────────────────────────
    private String emergencyContactName;
    private CatalogItem emergencyContactRelationship;
    private String emergencyContactPhone;
    private String emergencyContactPhone2;

    // ─── Dirección ────────────────────────────────────────────────────────────
    private String streetName;
    private String streetNumber;
    private String postalCode;
    private String department;
    private String village;
    private String block;
    private CatalogItem region;
    private CatalogItem city;
    private CatalogItem commune;

    // ─── Previsión y Salud ────────────────────────────────────────────────────
    private CatalogItem expat;
    private CatalogItem nationality;
    private CatalogItem familyAllowanceTier;
    private CatalogItem retirementStatus;
    private String isapreFun;
    private CatalogItem pensionStatus;
    private CatalogItem afp;
    private CatalogItem healthInsurance;
    private CatalogItem healthInsuranceTariff;
    private String healthInsuranceUF;
    private String healthInsurancePesos;

    // ─── Forma de Pago ────────────────────────────────────────────────────────
    private CatalogItem paymentMethod;
    private CatalogItem bank;
    private String bankAccount;

    // ─── Datos Organizacionales ───────────────────────────────────────────────
    private CatalogItem status;

    // ─── Otros Datos ─────────────────────────────────────────────────────────
    private String clothingSize;
    private String shoeSize;
    private String pantSize;
    private Boolean active;
    private Boolean rehireEligible;

    // ─── Auditoría ────────────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Datos del User vinculado (mcsv-user via Feign) ───────────────────────
    private String username;
    private String userEmail;
    private Boolean userEnabled;

    // ─── Contrato ─────────────────────────────────────────────────────────────
    private Boolean hasContract;

    // ─── Solicitud RRHH ───────────────────────────────────────────────────────
    private Long requestId;
}
