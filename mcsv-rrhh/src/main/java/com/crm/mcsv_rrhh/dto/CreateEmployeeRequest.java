package com.crm.mcsv_rrhh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEmployeeRequest {

    // ─── Vínculo con mcsv-user ────────────────────────────────────────────────
    @NotNull(message = "userId es requerido")
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
    private Long statusId;

    // ─── Otros Datos ──────────────────────────────────────────────────────────
    private String clothingSize;
    private String shoeSize;
    private String pantSize;
    private Integer flexlineId;
    private Boolean rehireEligible;
}
