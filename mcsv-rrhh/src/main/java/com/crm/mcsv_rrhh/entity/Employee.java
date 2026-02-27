package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un colaborador (empleado) dentro del módulo de RRHH.
 * Se vincula en relación 1:1 con el User de mcsv-user mediante userId.
 * Los catálogos se referencian por ID (Long) para mantener autonomía del microservicio.
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Vínculo 1:1 con mcsv-user ───────────────────────────────────────────
    /**
     * ID del User en mcsv-user. Garantiza la relación 1:1 con la restricción UNIQUE.
     */
    @Column(unique = true)
    private Long userId;

    // ─── Datos Personales ─────────────────────────────────────────────────────
    private String identification;
    private Long identificationTypeId;

    private String firstName;
    private String paternalLastName;
    private String maternalLastName;

    private LocalDate birthDate;

    private Long genderId;
    private Long maritalStatusId;
    private Long educationLevelId;
    private Long driverLicenseId;
    private Long professionId;

    // ─── Datos de Contacto ────────────────────────────────────────────────────
    private String personalEmail;
    private String corporateEmail;
    private String phone;
    private String phone2;

    // ─── Contacto de Emergencia ───────────────────────────────────────────────
    private String emergencyContactName;
    private Long emergencyContactRelationshipId;
    private String emergencyContactPhone;
    private String emergencyContactPhone2;

    // ─── Dirección ────────────────────────────────────────────────────────────
    private String streetName;
    private String streetNumber;
    private String postalCode;
    private String department;
    private String village;
    private String block;

    private Long regionId;
    private Long cityId;
    private Long communeId;

    // ─── Previsión y Salud ────────────────────────────────────────────────────
    private Long expatId;
    private Long nationalityId;
    private Long familyAllowanceTierId;
    private Long retirementStatusId;

    private String isapreFun;
    private Long pensionStatusId;
    private Long afpId;
    private Long healthInsuranceId;
    private Long healthInsuranceTariffId;
    private String healthInsuranceUF;
    private String healthInsurancePesos;

    // ─── Forma de Pago ────────────────────────────────────────────────────────
    private Long paymentMethodId;
    private Long bankId;
    private String bankAccount;

    // ─── Datos Organizacionales ───────────────────────────────────────────────
    private Long companyId;

    // ─── Otros Datos ─────────────────────────────────────────────────────────
    private String clothingSize;
    private String shoeSize;
    private String pantSize;

    private Integer flexlineId;

    /**
     * Indica si el colaborador está activo (empleado actualmente).
     */
    @Builder.Default
    private Boolean active = true;

    /**
     * ID del estado en el flujo de aprobación RRHH.
     * Estados: Pendiente aprobación, Aprobado, Rechazado, etc.
     */
    private Long statusId;

    /**
     * Indica si es elegible para recontratación según su historial.
     */
    @Builder.Default
    private Boolean rehireEligible = true;

    // ─── Auditoría ────────────────────────────────────────────────────────────
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
