package com.crm.mcsv_rrhh.service.impl;

import com.crm.mcsv_rrhh.client.UserClient;
import com.crm.mcsv_rrhh.dto.BulkImportResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.CreateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.EmployeeDetailResponse;
import com.crm.mcsv_rrhh.dto.EmployeeResponse;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.UpdateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.UserDTO;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.exception.DuplicateResourceException;
import com.crm.mcsv_rrhh.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.*;
import com.crm.mcsv_rrhh.service.EmployeeService;
import com.crm.mcsv_rrhh.service.HRRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserClient userClient;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final HRRequestRepository hrRequestRepository;
    private final HRRequestService hrRequestService;
    private final ObjectMapper objectMapper;
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
    private final ContractRepository contractRepository;

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
        HRRequest req = hrRequestService.createForEmployee(saved.getId(), "Trabajador", "CREATE", null);
        return toDetailResponse(saved, null, req.getId());
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
        try {
            String proposedData = objectMapper.writeValueAsString(request);
            HRRequest req = hrRequestService.createForEmployee(employee.getId(), "Trabajador", "UPDATE", proposedData);
            UserDTO user = fetchUser(employee.getUserId());
            return toDetailResponse(employee, user, req.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la solicitud de actualización", e);
        }
    }

    @Override
    public EmployeeDetailResponse getEmployeeById(Long id) {
        Employee employee = findOrThrow(id);
        UserDTO user = fetchUser(employee.getUserId());
        Long reqId = hrRequestRepository.findTopByIdModuleOrderByCreatedAtDesc(id)
                .map(HRRequest::getId).orElse(null);
        return toDetailResponse(employee, user, reqId);
    }

    @Override
    public EmployeeDetailResponse getEmployeeByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para userId: " + userId));
        UserDTO user = fetchUser(userId);
        Long reqId = hrRequestRepository.findTopByIdModuleOrderByCreatedAtDesc(employee.getId())
                .map(HRRequest::getId).orElse(null);
        return toDetailResponse(employee, user, reqId);
    }

    @Override
    public Page<EmployeeResponse> filterEmployees(String search, Boolean active, Long statusId,
                                                   java.time.LocalDate createdFrom, java.time.LocalDate createdTo,
                                                   Pageable pageable) {
        Long rejectedStatusId = employeeStatusRepository.findByName("Rechazado")
                .map(s -> s.getId()).orElse(null);
        Specification<Employee> spec = EmployeeSpecification.withFilters(search, active, rejectedStatusId, statusId, createdFrom, createdTo);
        Map<Long, String> statusMap = employeeStatusRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        s -> s.getId(),
                        s -> s.getName()
                ));
        return employeeRepository.findAll(spec, pageable).map(e -> toResponse(e, statusMap));
    }

    @Override
    public Map<String, Long> getEmployeeStats() {
        Long rejectedStatusId = employeeStatusRepository.findByName("Rechazado")
                .map(s -> s.getId()).orElse(null);
        long total  = rejectedStatusId != null
                ? employeeRepository.countByStatusIdNot(rejectedStatusId)
                : employeeRepository.count();
        long active = rejectedStatusId != null
                ? employeeRepository.countByActiveTrueAndStatusIdNot(rejectedStatusId)
                : employeeRepository.countByActiveTrue();
        return Map.of("total", total, "active", active);
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

    @Override
    public List<EmployeeService.EmployeeSelectItem> getEmployeesWithoutContract() {
        Long approvedStatusId = employeeStatusRepository.findByName("Aprobado")
                .map(s -> s.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: Aprobado"));

        List<Long> employeeIdsWithContract = contractRepository.findAll().stream()
                .map(c -> c.getEmployeeId())
                .distinct()
                .toList();

        List<Employee> employees = employeeIdsWithContract.isEmpty()
                ? employeeRepository.findByActiveTrueAndStatusId(approvedStatusId)
                : employeeRepository.findByActiveTrueAndStatusIdAndIdNotIn(approvedStatusId, employeeIdsWithContract);

        return employees.stream()
                .map(e -> new EmployeeService.EmployeeSelectItem(
                        e.getId(),
                        e.getFirstName() + " " + e.getPaternalLastName()))
                .toList();
    }

    @Override
    public List<EmployeeService.EmployeeSelectItem> getSupervisors() {
        try {
            List<Long> userIds = userClient.getSupervisors().stream().map(u -> u.getId()).toList();
            if (userIds.isEmpty()) return List.of();
            return employeeRepository.findByUserIdIn(userIds).stream()
                    .map(e -> new EmployeeService.EmployeeSelectItem(e.getId(), e.getFirstName() + " " + e.getPaternalLastName()))
                    .toList();
        } catch (Exception e) {
            log.warn("No se pudieron obtener supervisores: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<EmployeeService.EmployeeSelectItem> getVisitors() {
        try {
            List<Long> userIds = userClient.getVisitors().stream().map(u -> u.getId()).toList();
            if (userIds.isEmpty()) return List.of();
            return employeeRepository.findByUserIdIn(userIds).stream()
                    .map(e -> new EmployeeService.EmployeeSelectItem(e.getId(), e.getFirstName() + " " + e.getPaternalLastName()))
                    .toList();
        } catch (Exception e) {
            log.warn("No se pudieron obtener visitadores: {}", e.getMessage());
            return List.of();
        }
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
                .hasContract(e.getHasContract())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    @Override
    public BulkImportResult importFromCsv(MultipartFile file) {
        List<BulkImportResult.RowError> errors = new ArrayList<>();
        int total = 0;
        int success = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return BulkImportResult.builder().total(0).success(0).failed(0).errors(errors).build();
            }
            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> idx = buildHeaderIndex(headers);

            int iRut      = idx.getOrDefault("rut", -1);
            int iFirst    = idx.getOrDefault("nombre", -1);
            int iPat      = idx.getOrDefault("apellido paterno", -1);
            int iMat      = idx.getOrDefault("apellido materno", -1);
            int iEmail    = idx.getOrDefault("email corporativo", -1);
            int iPhone    = idx.getOrDefault("teléfono", idx.getOrDefault("telefono", -1));

            if (iRut < 0 && iFirst < 0) {
                errors.add(new BulkImportResult.RowError(1, "No se encontraron columnas reconocidas en el header"));
                return BulkImportResult.builder().total(0).success(0).failed(1).errors(errors).build();
            }

            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                total++;
                try {
                    String[] cols = parseCsvLine(line);

                    CreateEmployeeRequest request = new CreateEmployeeRequest();
                    request.setIdentification(col(cols, iRut).isEmpty() ? null : col(cols, iRut));
                    request.setFirstName(col(cols, iFirst).isEmpty() ? null : col(cols, iFirst));
                    request.setPaternalLastName(col(cols, iPat).isEmpty() ? null : col(cols, iPat));
                    request.setMaternalLastName(col(cols, iMat).isEmpty() ? null : col(cols, iMat));
                    request.setCorporateEmail(col(cols, iEmail).isEmpty() ? null : col(cols, iEmail));
                    request.setPhone(col(cols, iPhone).isEmpty() ? null : col(cols, iPhone));

                    createEmployee(request);
                    success++;
                } catch (Exception e) {
                    errors.add(new BulkImportResult.RowError(row, e.getMessage()));
                }
            }
        } catch (Exception e) {
            log.error("Error reading CSV file for employees", e);
            errors.add(new BulkImportResult.RowError(0, "Error leyendo el archivo: " + e.getMessage()));
        }

        return BulkImportResult.builder()
                .total(total)
                .success(success)
                .failed(errors.size())
                .errors(errors)
                .build();
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private Map<String, Integer> buildHeaderIndex(String[] headers) {
        Map<String, Integer> idx = new java.util.HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            idx.put(headers[i].trim().toLowerCase(), i);
        }
        return idx;
    }

    private String col(String[] cols, int index) {
        if (index < 0 || index >= cols.length) return "";
        return cols[index].trim();
    }

    @Override
    public byte[] exportCsv() {
        Map<Long, String> statusMap = employeeStatusRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(s -> s.getId(), s -> s.getName()));

        StringBuilder csv = new StringBuilder();
        csv.append("ID,RUT,Nombre,Apellido Paterno,Apellido Materno,Email Corporativo,Teléfono,Estado,Activo,Fecha Creación\n");

        employeeRepository.findAll().forEach(e -> csv
                .append(e.getId()).append(",")
                .append(escape(e.getIdentification())).append(",")
                .append(escape(e.getFirstName())).append(",")
                .append(escape(e.getPaternalLastName())).append(",")
                .append(escape(e.getMaternalLastName())).append(",")
                .append(escape(e.getCorporateEmail())).append(",")
                .append(escape(e.getPhone())).append(",")
                .append(escape(e.getStatusId() != null ? statusMap.get(e.getStatusId()) : "")).append(",")
                .append(e.getActive()).append(",")
                .append(formatDate(e.getCreatedAt())).append("\n"));

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String formatDate(java.time.LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }

    private EmployeeDetailResponse toDetailResponse(Employee e, UserDTO user, Long requestId) {
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
