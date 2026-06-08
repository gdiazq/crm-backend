package com.crm.mcsv_rrhh.mapper.employee;

import com.crm.mcsv_rrhh.client.dto.UserDTO;
import com.crm.mcsv_rrhh.dto.employee.CreateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.employee.EmployeeDetailResponse;
import com.crm.mcsv_rrhh.dto.employee.EmployeeResponse;
import com.crm.mcsv_rrhh.dto.employee.UpdateEmployeeRequest;
import com.crm.mcsv_rrhh.entity.employee.Employee;
import com.crm.mcsv_rrhh.mapper.shared.CatalogResolver;
import com.crm.mcsv_rrhh.repository.afp.AfpRepository;
import com.crm.mcsv_rrhh.repository.bank.BankRepository;
import com.crm.mcsv_rrhh.repository.city.CityRepository;
import com.crm.mcsv_rrhh.repository.commune.CommuneRepository;
import com.crm.mcsv_rrhh.repository.driverlicense.DriverLicenseRepository;
import com.crm.mcsv_rrhh.repository.educationlevel.EducationLevelRepository;
import com.crm.mcsv_rrhh.repository.emergencycontactrelationship.EmergencyContactRelationshipRepository;
import com.crm.mcsv_rrhh.repository.employee.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.expat.ExpatRepository;
import com.crm.mcsv_rrhh.repository.familyallowancetier.FamilyAllowanceTierRepository;
import com.crm.mcsv_rrhh.repository.gender.GenderRepository;
import com.crm.mcsv_rrhh.repository.healthinsurance.HealthInsuranceRepository;
import com.crm.mcsv_rrhh.repository.healthinsurancetariff.HealthInsuranceTariffRepository;
import com.crm.mcsv_rrhh.repository.identificationtype.IdentificationTypeRepository;
import com.crm.mcsv_rrhh.repository.maritalstatus.MaritalStatusRepository;
import com.crm.mcsv_rrhh.repository.nationality.NationalityRepository;
import com.crm.mcsv_rrhh.repository.paymentmethod.PaymentMethodRepository;
import com.crm.mcsv_rrhh.repository.pensionstatus.PensionStatusRepository;
import com.crm.mcsv_rrhh.repository.profession.ProfessionRepository;
import com.crm.mcsv_rrhh.repository.region.RegionRepository;
import com.crm.mcsv_rrhh.repository.retirementstatus.RetirementStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mapea entidades Employee a sus DTOs de respuesta. Concentra aquí la resolución de los
 * catálogos por id (gender, comuna, afp, etc.) para que el service no tenga que inyectarlos.
 */
@Component
@RequiredArgsConstructor
public class EmployeeMapper {

    private final IdentificationTypeRepository identificationTypeRepository;
    private final GenderRepository genderRepository;
    private final MaritalStatusRepository maritalStatusRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final DriverLicenseRepository driverLicenseRepository;
    private final ProfessionRepository professionRepository;
    private final EmergencyContactRelationshipRepository emergencyContactRelationshipRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    private final CommuneRepository communeRepository;
    private final ExpatRepository expatRepository;
    private final NationalityRepository nationalityRepository;
    private final FamilyAllowanceTierRepository familyAllowanceTierRepository;
    private final RetirementStatusRepository retirementStatusRepository;
    private final PensionStatusRepository pensionStatusRepository;
    private final AfpRepository afpRepository;
    private final HealthInsuranceRepository healthInsuranceRepository;
    private final HealthInsuranceTariffRepository healthInsuranceTariffRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final BankRepository bankRepository;
    private final EmployeeStatusRepository employeeStatusRepository;

    public Employee toEntity(CreateEmployeeRequest request, Long pendingStatusId) {
        return Employee.builder()
                .identification(request.getIdentification())
                .identificationTypeId(request.getIdentificationTypeId())
                .firstName(request.getFirstName())
                .paternalLastName(request.getPaternalLastName())
                .maternalLastName(request.getMaternalLastName())
                .birthDate(request.getBirthDate())
                .genderId(request.getGenderId())
                .maritalStatusId(request.getMaritalStatusId())
                .educationLevelId(request.getEducationLevelId())
                .driverLicenseId(request.getDriverLicenseId())
                .professionId(request.getProfessionId())
                .personalEmail(request.getPersonalEmail())
                .corporateEmail(request.getCorporateEmail())
                .phone(request.getPhone())
                .phone2(request.getPhone2())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactRelationshipId(request.getEmergencyContactRelationshipId())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .emergencyContactPhone2(request.getEmergencyContactPhone2())
                .streetName(request.getStreetName())
                .streetNumber(request.getStreetNumber())
                .postalCode(request.getPostalCode())
                .department(request.getDepartment())
                .village(request.getVillage())
                .block(request.getBlock())
                .regionId(request.getRegionId())
                .cityId(request.getCityId())
                .communeId(request.getCommuneId())
                .expatId(request.getExpatId())
                .nationalityId(request.getNationalityId())
                .familyAllowanceTierId(request.getFamilyAllowanceTierId())
                .retirementStatusId(request.getRetirementStatusId())
                .isapreFun(request.getIsapreFun())
                .pensionStatusId(request.getPensionStatusId())
                .afpId(request.getAfpId())
                .healthInsuranceId(request.getHealthInsuranceId())
                .healthInsuranceTariffId(request.getHealthInsuranceTariffId())
                .healthInsuranceUF(request.getHealthInsuranceUF())
                .healthInsurancePesos(request.getHealthInsurancePesos())
                .paymentMethodId(request.getPaymentMethodId())
                .bankId(request.getBankId())
                .bankAccount(request.getBankAccount())
                .clothingSize(request.getClothingSize())
                .shoeSize(request.getShoeSize())
                .pantSize(request.getPantSize())
                .statusId(pendingStatusId)
                .rehireEligible(request.getRehireEligible() != null ? request.getRehireEligible() : true)
                .active(true)
                .build();
    }

    /** Copia los campos de una solicitud de actualización aprobada sobre el empleado existente. */
    public void applyUpdate(Employee employee, UpdateEmployeeRequest proposed) {
        employee.setIdentification(proposed.getIdentification());
        employee.setIdentificationTypeId(proposed.getIdentificationTypeId());
        employee.setFirstName(proposed.getFirstName());
        employee.setPaternalLastName(proposed.getPaternalLastName());
        employee.setMaternalLastName(proposed.getMaternalLastName());
        employee.setBirthDate(proposed.getBirthDate());
        employee.setGenderId(proposed.getGenderId());
        employee.setMaritalStatusId(proposed.getMaritalStatusId());
        employee.setEducationLevelId(proposed.getEducationLevelId());
        employee.setDriverLicenseId(proposed.getDriverLicenseId());
        employee.setProfessionId(proposed.getProfessionId());
        employee.setPersonalEmail(proposed.getPersonalEmail());
        employee.setCorporateEmail(proposed.getCorporateEmail());
        employee.setPhone(proposed.getPhone());
        employee.setPhone2(proposed.getPhone2());
        employee.setEmergencyContactName(proposed.getEmergencyContactName());
        employee.setEmergencyContactRelationshipId(proposed.getEmergencyContactRelationshipId());
        employee.setEmergencyContactPhone(proposed.getEmergencyContactPhone());
        employee.setEmergencyContactPhone2(proposed.getEmergencyContactPhone2());
        employee.setStreetName(proposed.getStreetName());
        employee.setStreetNumber(proposed.getStreetNumber());
        employee.setPostalCode(proposed.getPostalCode());
        employee.setDepartment(proposed.getDepartment());
        employee.setVillage(proposed.getVillage());
        employee.setBlock(proposed.getBlock());
        employee.setRegionId(proposed.getRegionId());
        employee.setCityId(proposed.getCityId());
        employee.setCommuneId(proposed.getCommuneId());
        employee.setExpatId(proposed.getExpatId());
        employee.setNationalityId(proposed.getNationalityId());
        employee.setFamilyAllowanceTierId(proposed.getFamilyAllowanceTierId());
        employee.setRetirementStatusId(proposed.getRetirementStatusId());
        employee.setIsapreFun(proposed.getIsapreFun());
        employee.setPensionStatusId(proposed.getPensionStatusId());
        employee.setAfpId(proposed.getAfpId());
        employee.setHealthInsuranceId(proposed.getHealthInsuranceId());
        employee.setHealthInsuranceTariffId(proposed.getHealthInsuranceTariffId());
        employee.setHealthInsuranceUF(proposed.getHealthInsuranceUF());
        employee.setHealthInsurancePesos(proposed.getHealthInsurancePesos());
        employee.setPaymentMethodId(proposed.getPaymentMethodId());
        employee.setBankId(proposed.getBankId());
        employee.setBankAccount(proposed.getBankAccount());
        employee.setClothingSize(proposed.getClothingSize());
        employee.setShoeSize(proposed.getShoeSize());
        employee.setPantSize(proposed.getPantSize());
        employee.setActive(proposed.getActive());
        employee.setRehireEligible(proposed.getRehireEligible());
    }

    public EmployeeResponse toResponse(Employee e, Map<Long, String> statusMap) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .identification(e.getIdentification())
                .firstName(e.getFirstName())
                .paternalLastName(e.getPaternalLastName())
                .maternalLastName(e.getMaternalLastName())
                .corporateEmail(e.getCorporateEmail())
                .phone(e.getPhone())
                .statusName(e.getStatusId() != null ? statusMap.get(e.getStatusId()) : null)
                .active(e.getActive())
                .rehireEligible(e.getRehireEligible())
                .hasContract(e.getHasContract())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public EmployeeDetailResponse toDetailResponse(Employee e, UserDTO user, Long requestId) {
        EmployeeDetailResponse.EmployeeDetailResponseBuilder builder = EmployeeDetailResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .identification(e.getIdentification())
                .identificationType(CatalogResolver.item(e.getIdentificationTypeId(), identificationTypeRepository))
                .firstName(e.getFirstName())
                .paternalLastName(e.getPaternalLastName())
                .maternalLastName(e.getMaternalLastName())
                .birthDate(e.getBirthDate())
                .gender(CatalogResolver.item(e.getGenderId(), genderRepository))
                .maritalStatus(CatalogResolver.item(e.getMaritalStatusId(), maritalStatusRepository))
                .educationLevel(CatalogResolver.item(e.getEducationLevelId(), educationLevelRepository))
                .driverLicense(CatalogResolver.item(e.getDriverLicenseId(), driverLicenseRepository))
                .profession(CatalogResolver.item(e.getProfessionId(), professionRepository))
                .personalEmail(e.getPersonalEmail())
                .corporateEmail(e.getCorporateEmail())
                .phone(e.getPhone())
                .phone2(e.getPhone2())
                .emergencyContactName(e.getEmergencyContactName())
                .emergencyContactRelationship(CatalogResolver.item(e.getEmergencyContactRelationshipId(), emergencyContactRelationshipRepository))
                .emergencyContactPhone(e.getEmergencyContactPhone())
                .emergencyContactPhone2(e.getEmergencyContactPhone2())
                .streetName(e.getStreetName())
                .streetNumber(e.getStreetNumber())
                .postalCode(e.getPostalCode())
                .department(e.getDepartment())
                .village(e.getVillage())
                .block(e.getBlock())
                .region(CatalogResolver.item(e.getRegionId(), regionRepository))
                .city(CatalogResolver.item(e.getCityId(), cityRepository))
                .commune(CatalogResolver.item(e.getCommuneId(), communeRepository))
                .expat(CatalogResolver.item(e.getExpatId(), expatRepository))
                .nationality(CatalogResolver.item(e.getNationalityId(), nationalityRepository))
                .familyAllowanceTier(CatalogResolver.item(e.getFamilyAllowanceTierId(), familyAllowanceTierRepository))
                .retirementStatus(CatalogResolver.item(e.getRetirementStatusId(), retirementStatusRepository))
                .isapreFun(e.getIsapreFun())
                .pensionStatus(CatalogResolver.item(e.getPensionStatusId(), pensionStatusRepository))
                .afp(CatalogResolver.item(e.getAfpId(), afpRepository))
                .healthInsurance(CatalogResolver.item(e.getHealthInsuranceId(), healthInsuranceRepository))
                .healthInsuranceTariff(CatalogResolver.item(e.getHealthInsuranceTariffId(), healthInsuranceTariffRepository))
                .healthInsuranceUF(e.getHealthInsuranceUF())
                .healthInsurancePesos(e.getHealthInsurancePesos())
                .paymentMethod(CatalogResolver.item(e.getPaymentMethodId(), paymentMethodRepository))
                .bank(CatalogResolver.item(e.getBankId(), bankRepository))
                .bankAccount(e.getBankAccount())
                .status(CatalogResolver.item(e.getStatusId(), employeeStatusRepository))
                .clothingSize(e.getClothingSize())
                .shoeSize(e.getShoeSize())
                .pantSize(e.getPantSize())
                .active(e.getActive())
                .rehireEligible(e.getRehireEligible())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt());

        if (user != null) {
            builder.username(user.getUsername())
                   .userEmail(user.getEmail())
                   .userEnabled(user.getEnabled());
        }

        builder.requestId(requestId);
        builder.hasContract(e.getHasContract());

        return builder.build();
    }
}
