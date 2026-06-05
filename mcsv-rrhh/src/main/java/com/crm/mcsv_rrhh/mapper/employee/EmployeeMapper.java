package com.crm.mcsv_rrhh.mapper.employee;

import com.crm.mcsv_rrhh.client.dto.UserDTO;
import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.employee.EmployeeDetailResponse;
import com.crm.mcsv_rrhh.dto.employee.EmployeeResponse;
import com.crm.mcsv_rrhh.entity.employee.Employee;
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
import org.springframework.data.jpa.repository.JpaRepository;
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
                .identificationType(resolve(e.getIdentificationTypeId(), identificationTypeRepository))
                .firstName(e.getFirstName())
                .paternalLastName(e.getPaternalLastName())
                .maternalLastName(e.getMaternalLastName())
                .birthDate(e.getBirthDate())
                .gender(resolve(e.getGenderId(), genderRepository))
                .maritalStatus(resolve(e.getMaritalStatusId(), maritalStatusRepository))
                .educationLevel(resolve(e.getEducationLevelId(), educationLevelRepository))
                .driverLicense(resolve(e.getDriverLicenseId(), driverLicenseRepository))
                .profession(resolve(e.getProfessionId(), professionRepository))
                .personalEmail(e.getPersonalEmail())
                .corporateEmail(e.getCorporateEmail())
                .phone(e.getPhone())
                .phone2(e.getPhone2())
                .emergencyContactName(e.getEmergencyContactName())
                .emergencyContactRelationship(resolve(e.getEmergencyContactRelationshipId(), emergencyContactRelationshipRepository))
                .emergencyContactPhone(e.getEmergencyContactPhone())
                .emergencyContactPhone2(e.getEmergencyContactPhone2())
                .streetName(e.getStreetName())
                .streetNumber(e.getStreetNumber())
                .postalCode(e.getPostalCode())
                .department(e.getDepartment())
                .village(e.getVillage())
                .block(e.getBlock())
                .region(resolve(e.getRegionId(), regionRepository))
                .city(resolve(e.getCityId(), cityRepository))
                .commune(resolve(e.getCommuneId(), communeRepository))
                .expat(resolve(e.getExpatId(), expatRepository))
                .nationality(resolve(e.getNationalityId(), nationalityRepository))
                .familyAllowanceTier(resolve(e.getFamilyAllowanceTierId(), familyAllowanceTierRepository))
                .retirementStatus(resolve(e.getRetirementStatusId(), retirementStatusRepository))
                .isapreFun(e.getIsapreFun())
                .pensionStatus(resolve(e.getPensionStatusId(), pensionStatusRepository))
                .afp(resolve(e.getAfpId(), afpRepository))
                .healthInsurance(resolve(e.getHealthInsuranceId(), healthInsuranceRepository))
                .healthInsuranceTariff(resolve(e.getHealthInsuranceTariffId(), healthInsuranceTariffRepository))
                .healthInsuranceUF(e.getHealthInsuranceUF())
                .healthInsurancePesos(e.getHealthInsurancePesos())
                .paymentMethod(resolve(e.getPaymentMethodId(), paymentMethodRepository))
                .bank(resolve(e.getBankId(), bankRepository))
                .bankAccount(e.getBankAccount())
                .status(resolve(e.getStatusId(), employeeStatusRepository))
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

    private <T> CatalogItem resolve(Long id, JpaRepository<T, Long> repo) {
        if (id == null) return null;
        return repo.findById(id)
                .map(e -> {
                    try {
                        var getName = e.getClass().getMethod("getName");
                        return new CatalogItem(id, (String) getName.invoke(e));
                    } catch (Exception ex) {
                        return new CatalogItem(id, null);
                    }
                })
                .orElse(null);
    }
}
