package com.crm.mcsv_rrhh.service.impl;

import com.crm.mcsv_rrhh.client.UserClient;
import com.crm.mcsv_rrhh.dto.CreateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.EmployeeDetailResponse;
import com.crm.mcsv_rrhh.dto.EmployeeResponse;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.UpdateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.UserDTO;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.exception.DuplicateResourceException;
import com.crm.mcsv_rrhh.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.EmployeeSpecification;
import com.crm.mcsv_rrhh.repository.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserClient userClient;
    private final EmployeeStatusRepository employeeStatusRepository;

    @Override
    public EmployeeDetailResponse createEmployee(CreateEmployeeRequest request) {
        Long pendingStatusId = employeeStatusRepository.findByName("Pendiente de revisión")
                .map(s -> s.getId())
                .orElse(null);

        Employee employee = Employee.builder()
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

        Employee saved = employeeRepository.save(employee);
        return toDetailResponse(saved, null);
    }

    @Override
    public void linkUser(Long id, Long userId) {
        Employee employee = findOrThrow(id);
        if (employeeRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("El usuario ya está vinculado a otro empleado");
        }
        UserDTO user = userClient.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + userId);
        }
        employee.setUserId(userId);
        employeeRepository.save(employee);
    }

    @Override
    public void unlinkUser(Long id) {
        Employee employee = findOrThrow(id);
        employee.setUserId(null);
        employeeRepository.save(employee);
    }

    @Override
    public EmployeeDetailResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = findOrThrow(id);

        if (request.getIdentification() != null)              employee.setIdentification(request.getIdentification());
        if (request.getIdentificationTypeId() != null)        employee.setIdentificationTypeId(request.getIdentificationTypeId());
        if (request.getFirstName() != null)                   employee.setFirstName(request.getFirstName());
        if (request.getPaternalLastName() != null)            employee.setPaternalLastName(request.getPaternalLastName());
        if (request.getMaternalLastName() != null)            employee.setMaternalLastName(request.getMaternalLastName());
        if (request.getBirthDate() != null)                   employee.setBirthDate(request.getBirthDate());
        if (request.getGenderId() != null)                    employee.setGenderId(request.getGenderId());
        if (request.getMaritalStatusId() != null)             employee.setMaritalStatusId(request.getMaritalStatusId());
        if (request.getEducationLevelId() != null)            employee.setEducationLevelId(request.getEducationLevelId());
        if (request.getDriverLicenseId() != null)             employee.setDriverLicenseId(request.getDriverLicenseId());
        if (request.getProfessionId() != null)                employee.setProfessionId(request.getProfessionId());
        if (request.getPersonalEmail() != null)               employee.setPersonalEmail(request.getPersonalEmail());
        if (request.getCorporateEmail() != null)              employee.setCorporateEmail(request.getCorporateEmail());
        if (request.getPhone() != null)                       employee.setPhone(request.getPhone());
        if (request.getPhone2() != null)                      employee.setPhone2(request.getPhone2());
        if (request.getEmergencyContactName() != null)        employee.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactRelationshipId() != null) employee.setEmergencyContactRelationshipId(request.getEmergencyContactRelationshipId());
        if (request.getEmergencyContactPhone() != null)       employee.setEmergencyContactPhone(request.getEmergencyContactPhone());
        if (request.getEmergencyContactPhone2() != null)      employee.setEmergencyContactPhone2(request.getEmergencyContactPhone2());
        if (request.getStreetName() != null)                  employee.setStreetName(request.getStreetName());
        if (request.getStreetNumber() != null)                employee.setStreetNumber(request.getStreetNumber());
        if (request.getPostalCode() != null)                  employee.setPostalCode(request.getPostalCode());
        if (request.getDepartment() != null)                  employee.setDepartment(request.getDepartment());
        if (request.getVillage() != null)                     employee.setVillage(request.getVillage());
        if (request.getBlock() != null)                       employee.setBlock(request.getBlock());
        if (request.getRegionId() != null)                    employee.setRegionId(request.getRegionId());
        if (request.getCityId() != null)                      employee.setCityId(request.getCityId());
        if (request.getCommuneId() != null)                   employee.setCommuneId(request.getCommuneId());
        if (request.getExpatId() != null)                     employee.setExpatId(request.getExpatId());
        if (request.getNationalityId() != null)               employee.setNationalityId(request.getNationalityId());
        if (request.getFamilyAllowanceTierId() != null)       employee.setFamilyAllowanceTierId(request.getFamilyAllowanceTierId());
        if (request.getRetirementStatusId() != null)          employee.setRetirementStatusId(request.getRetirementStatusId());
        if (request.getIsapreFun() != null)                   employee.setIsapreFun(request.getIsapreFun());
        if (request.getPensionStatusId() != null)             employee.setPensionStatusId(request.getPensionStatusId());
        if (request.getAfpId() != null)                       employee.setAfpId(request.getAfpId());
        if (request.getHealthInsuranceId() != null)           employee.setHealthInsuranceId(request.getHealthInsuranceId());
        if (request.getHealthInsuranceTariffId() != null)     employee.setHealthInsuranceTariffId(request.getHealthInsuranceTariffId());
        if (request.getHealthInsuranceUF() != null)           employee.setHealthInsuranceUF(request.getHealthInsuranceUF());
        if (request.getHealthInsurancePesos() != null)        employee.setHealthInsurancePesos(request.getHealthInsurancePesos());
        if (request.getPaymentMethodId() != null)             employee.setPaymentMethodId(request.getPaymentMethodId());
        if (request.getBankId() != null)                      employee.setBankId(request.getBankId());
        if (request.getBankAccount() != null)                 employee.setBankAccount(request.getBankAccount());
        if (request.getClothingSize() != null)                employee.setClothingSize(request.getClothingSize());
        if (request.getShoeSize() != null)                    employee.setShoeSize(request.getShoeSize());
        if (request.getPantSize() != null)                    employee.setPantSize(request.getPantSize());
        if (request.getStatusId() != null)                    employee.setStatusId(request.getStatusId());
        if (request.getActive() != null)                      employee.setActive(request.getActive());
        if (request.getRehireEligible() != null)              employee.setRehireEligible(request.getRehireEligible());

        Employee saved = employeeRepository.save(employee);
        UserDTO user = fetchUser(saved.getUserId());
        return toDetailResponse(saved, user);
    }

    @Override
    public EmployeeDetailResponse getEmployeeById(Long id) {
        Employee employee = findOrThrow(id);
        UserDTO user = fetchUser(employee.getUserId());
        return toDetailResponse(employee, user);
    }

    @Override
    public EmployeeDetailResponse getEmployeeByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para userId: " + userId));
        UserDTO user = fetchUser(userId);
        return toDetailResponse(employee, user);
    }

    @Override
    public Page<EmployeeResponse> filterEmployees(String search, Boolean active, Pageable pageable) {
        Specification<Employee> spec = EmployeeSpecification.withFilters(search, active);
        Map<Long, String> statusMap = employeeStatusRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        s -> s.getId(),
                        s -> s.getName()
                ));
        return employeeRepository.findAll(spec, pageable).map(e -> toResponse(e, statusMap));
    }

    @Override
    public Map<String, Long> getEmployeeStats() {
        return Map.of(
                "total", employeeRepository.count(),
                "active", employeeRepository.countByActiveTrue()
        );
    }

    @Override
    public void updateStatus(Long id, Boolean active) {
        Employee employee = findOrThrow(id);
        employee.setActive(active);
        employeeRepository.save(employee);
    }

    @Override
    public PagedResponse<UserDTO> getAvailableUsersForEmployee(String search, int page, int size) {
        List<Long> linkedUserIds = employeeRepository.findAllUserIds();
        return userClient.getAvailableForEmployee(search, linkedUserIds, page, size);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Employee findOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id: " + id));
    }

    private UserDTO fetchUser(Long userId) {
        if (userId == null) return null;
        try {
            return userClient.getUserById(userId);
        } catch (Exception e) {
            log.warn("No se pudo obtener el usuario con id {}: {}", userId, e.getMessage());
            return null;
        }
    }

    private EmployeeResponse toResponse(Employee e, Map<Long, String> statusMap) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .identification(e.getIdentification())
                .firstName(e.getFirstName())
                .paternalLastName(e.getPaternalLastName())
                .maternalLastName(e.getMaternalLastName())
                .corporateEmail(e.getCorporateEmail())
                .phone(e.getPhone())
                .statusName(e.getStatusId() != null ? statusMap.get(e.getStatusId()) : null)
                .active(e.getActive())
                .rehireEligible(e.getRehireEligible())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private EmployeeDetailResponse toDetailResponse(Employee e, UserDTO user) {
        EmployeeDetailResponse.EmployeeDetailResponseBuilder builder = EmployeeDetailResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .identification(e.getIdentification())
                .identificationTypeId(e.getIdentificationTypeId())
                .firstName(e.getFirstName())
                .paternalLastName(e.getPaternalLastName())
                .maternalLastName(e.getMaternalLastName())
                .birthDate(e.getBirthDate())
                .genderId(e.getGenderId())
                .maritalStatusId(e.getMaritalStatusId())
                .educationLevelId(e.getEducationLevelId())
                .driverLicenseId(e.getDriverLicenseId())
                .professionId(e.getProfessionId())
                .personalEmail(e.getPersonalEmail())
                .corporateEmail(e.getCorporateEmail())
                .phone(e.getPhone())
                .phone2(e.getPhone2())
                .emergencyContactName(e.getEmergencyContactName())
                .emergencyContactRelationshipId(e.getEmergencyContactRelationshipId())
                .emergencyContactPhone(e.getEmergencyContactPhone())
                .emergencyContactPhone2(e.getEmergencyContactPhone2())
                .streetName(e.getStreetName())
                .streetNumber(e.getStreetNumber())
                .postalCode(e.getPostalCode())
                .department(e.getDepartment())
                .village(e.getVillage())
                .block(e.getBlock())
                .regionId(e.getRegionId())
                .cityId(e.getCityId())
                .communeId(e.getCommuneId())
                .expatId(e.getExpatId())
                .nationalityId(e.getNationalityId())
                .familyAllowanceTierId(e.getFamilyAllowanceTierId())
                .retirementStatusId(e.getRetirementStatusId())
                .isapreFun(e.getIsapreFun())
                .pensionStatusId(e.getPensionStatusId())
                .afpId(e.getAfpId())
                .healthInsuranceId(e.getHealthInsuranceId())
                .healthInsuranceTariffId(e.getHealthInsuranceTariffId())
                .healthInsuranceUF(e.getHealthInsuranceUF())
                .healthInsurancePesos(e.getHealthInsurancePesos())
                .paymentMethodId(e.getPaymentMethodId())
                .bankId(e.getBankId())
                .bankAccount(e.getBankAccount())
                .statusId(e.getStatusId())
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

        return builder.build();
    }
}
