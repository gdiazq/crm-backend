package com.crm.mcsv_rrhh.service.employee.impl;

import com.crm.mcsv_rrhh.client.UserClient;
import com.crm.common.dto.BulkImportResult;
import com.crm.common.util.CsvUtil;
import com.crm.mcsv_rrhh.entity.contract.Contract;
import com.crm.mcsv_rrhh.entity.contract.ContractStatus;
import com.crm.mcsv_rrhh.entity.employee.EmployeeStatus;
import com.crm.mcsv_rrhh.enums.contract.ContractStatusName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.employee.CreateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.employee.EmployeeDetailResponse;
import com.crm.mcsv_rrhh.dto.employee.EmployeeResponse;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.employee.UpdateEmployeeRequest;
import com.crm.mcsv_rrhh.client.dto.UserDTO;
import com.crm.mcsv_rrhh.entity.employee.Employee;
import com.crm.mcsv_rrhh.entity.hrrequest.HRRequest;
import com.crm.mcsv_rrhh.enums.hrrequest.HRRequestTypeName;
import com.crm.mcsv_rrhh.enums.hrrequest.RequestStatus;
import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.service.employee.EmployeeService;
import com.crm.mcsv_rrhh.service.hrrequest.HRRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.crm.mcsv_rrhh.repository.contract.ContractRepository;
import com.crm.mcsv_rrhh.repository.contract.ContractStatusRepository;
import com.crm.mcsv_rrhh.repository.employee.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.employee.EmployeeSpecification;
import com.crm.mcsv_rrhh.repository.employee.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.hrrequest.HRRequestRepository;
import com.crm.mcsv_rrhh.mapper.employee.EmployeeMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserClient userClient;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final HRRequestRepository hrRequestRepository;
    private final HRRequestService hrRequestService;
    private final ObjectMapper objectMapper;
    private final ContractRepository contractRepository;
    private final EmployeeMapper mapper;

    @Override
    public EmployeeDetailResponse createEmployee(CreateEmployeeRequest request) {
        Long pendingStatusId = employeeStatusRepository.findByName(RequestStatus.PENDING_REVIEW.getDisplayName())
                .map(EmployeeStatus::getId)
                .orElse(null);

        Employee employee = mapper.toEntity(request, pendingStatusId);

        Employee saved = employeeRepository.save(employee);
        HRRequest req = hrRequestService.createForEmployee(saved.getId(), HRRequestTypeName.EMPLOYEE.getDisplayName(), "CREATE", null);
        return mapper.toDetailResponse(saved, null, req.getId());
    }

    @Override
    public void updateLinkedUser(Long id, Long userId) {
        Employee employee = findOrThrow(id);
        if (userId != null) {
            if (employeeRepository.existsByUserId(userId)) {
                throw new DuplicateResourceException("El usuario ya está vinculado a otro empleado");
            }
            UserDTO user = userClient.getUserById(userId);
            if (user == null) {
                throw new ResourceNotFoundException("Usuario no encontrado con id: " + userId);
            }
        }
        employee.setUserId(userId);
        employeeRepository.save(employee);
    }

    @Override
    public EmployeeDetailResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = findOrThrow(id);
        try {
            String proposedData = objectMapper.writeValueAsString(request);
            HRRequest req = hrRequestService.createForEmployee(employee.getId(), HRRequestTypeName.EMPLOYEE.getDisplayName(), "UPDATE", proposedData);
            UserDTO user = fetchUser(employee.getUserId());
            return mapper.toDetailResponse(employee, user, req.getId());
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
        return mapper.toDetailResponse(employee, user, reqId);
    }

    @Override
    public EmployeeDetailResponse getEmployeeByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para userId: " + userId));
        UserDTO user = fetchUser(userId);
        Long reqId = hrRequestRepository.findTopByIdModuleOrderByCreatedAtDesc(employee.getId())
                .map(HRRequest::getId).orElse(null);
        return mapper.toDetailResponse(employee, user, reqId);
    }

    @Override
    public Page<EmployeeResponse> filterEmployees(String search, Boolean active, Long statusId,
                                                   java.time.LocalDate createdFrom, java.time.LocalDate createdTo,
                                                   Pageable pageable) {
        Long rejectedStatusId = employeeStatusRepository.findByName(RequestStatus.REJECTED.getDisplayName())
                .map(EmployeeStatus::getId).orElse(null);
        Specification<Employee> spec = EmployeeSpecification.withFilters(search, active, rejectedStatusId, statusId, createdFrom, createdTo);
        Map<Long, String> statusMap = employeeStatusRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        EmployeeStatus::getId,
                        EmployeeStatus::getName
                ));
        return employeeRepository.findAll(spec, pageable).map(e -> mapper.toResponse(e, statusMap));
    }

    @Override
    public Map<String, Long> getEmployeeStats() {
        Long rejectedStatusId = employeeStatusRepository.findByName(RequestStatus.REJECTED.getDisplayName())
                .map(EmployeeStatus::getId).orElse(null);
        Specification<Employee> baseSpec = EmployeeSpecification.withFilters(
                null, null, rejectedStatusId, null, null, null);
        Specification<Employee> activeSpec = EmployeeSpecification.withFilters(
                null, true, rejectedStatusId, null, null, null);
        long total  = employeeRepository.count(baseSpec);
        long active = employeeRepository.count(activeSpec);
        return Map.of("total", total, "active", active);
    }

    @Override
    public void updateStatus(Long id, Boolean active) {
        Employee employee = findOrThrow(id);
        employee.setActive(active);
        employeeRepository.save(employee);
    }

    @Override
    public List<CatalogItem> getAvailableUsersForEmployee(String search) {
        List<Long> linkedUserIds = employeeRepository.findAllUserIds();
        return userClient.getAvailableForEmployee(search, linkedUserIds);
    }

    @Override
    public List<EmployeeService.EmployeeSelectItem> getEmployeesWithoutContract() {
        Long approvedStatusId = employeeStatusRepository.findByName(RequestStatus.APPROVED.getDisplayName())
                .map(EmployeeStatus::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: Aprobado"));
        Long activeContractStatusId = contractStatusRepository.findByName(ContractStatusName.ACTIVE.getDisplayName())
                .map(ContractStatus::getId).orElse(null);

        List<Long> employeeIdsWithActiveContract = activeContractStatusId == null
                ? List.of()
                : contractRepository.findEmployeeIdsByContractStatusId(activeContractStatusId);

        List<Employee> employees = employeeIdsWithActiveContract.isEmpty()
                ? employeeRepository.findByActiveTrueAndStatusId(approvedStatusId)
                : employeeRepository.findByActiveTrueAndStatusIdAndIdNotIn(approvedStatusId, employeeIdsWithActiveContract);

        return employees.stream()
                .map(e -> new EmployeeService.EmployeeSelectItem(
                        e.getId(),
                        e.getFirstName() + " " + e.getPaternalLastName()))
                .toList();
    }

    @Override
    public List<EmployeeService.EmployeeSelectItem> getEmployeesWithContract() {
        Long approvedStatusId = employeeStatusRepository.findByName(RequestStatus.APPROVED.getDisplayName())
                .map(EmployeeStatus::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: Aprobado"));
        Long activeContractStatusId = contractStatusRepository.findByName(ContractStatusName.ACTIVE.getDisplayName())
                .map(ContractStatus::getId).orElse(null);

        if (activeContractStatusId == null) return List.of();

        List<Long> employeeIdsWithActiveContract =
                contractRepository.findEmployeeIdsByContractStatusId(activeContractStatusId);

        if (employeeIdsWithActiveContract.isEmpty()) return List.of();

        return employeeRepository.findByActiveTrueAndStatusIdAndIdIn(approvedStatusId, employeeIdsWithActiveContract)
                .stream()
                .map(e -> new EmployeeService.EmployeeSelectItem(
                        e.getId(),
                        e.getFirstName() + " " + e.getPaternalLastName()))
                .toList();
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
            String[] headers = CsvUtil.parseLine(headerLine);
            Map<String, Integer> idx = CsvUtil.headerIndex(headers);

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
                    String[] cols = CsvUtil.parseLine(line);

                    CreateEmployeeRequest request = new CreateEmployeeRequest();
                    request.setIdentification(CsvUtil.colOrEmpty(cols, iRut).isEmpty() ? null : CsvUtil.colOrEmpty(cols, iRut));
                    request.setFirstName(CsvUtil.colOrEmpty(cols, iFirst).isEmpty() ? null : CsvUtil.colOrEmpty(cols, iFirst));
                    request.setPaternalLastName(CsvUtil.colOrEmpty(cols, iPat).isEmpty() ? null : CsvUtil.colOrEmpty(cols, iPat));
                    request.setMaternalLastName(CsvUtil.colOrEmpty(cols, iMat).isEmpty() ? null : CsvUtil.colOrEmpty(cols, iMat));
                    request.setCorporateEmail(CsvUtil.colOrEmpty(cols, iEmail).isEmpty() ? null : CsvUtil.colOrEmpty(cols, iEmail));
                    request.setPhone(CsvUtil.colOrEmpty(cols, iPhone).isEmpty() ? null : CsvUtil.colOrEmpty(cols, iPhone));

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

    @Override
    public byte[] exportCsv() {
        Map<Long, String> statusMap = employeeStatusRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(EmployeeStatus::getId, EmployeeStatus::getName));

        StringBuilder csv = new StringBuilder();
        csv.append("ID,RUT,Nombre,Apellido Paterno,Apellido Materno,Email Corporativo,Teléfono,Estado,Activo,Fecha Creación\n");

        employeeRepository.findAll().forEach(e -> csv
                .append(e.getId()).append(",")
                .append(CsvUtil.escape(e.getIdentification())).append(",")
                .append(CsvUtil.escape(e.getFirstName())).append(",")
                .append(CsvUtil.escape(e.getPaternalLastName())).append(",")
                .append(CsvUtil.escape(e.getMaternalLastName())).append(",")
                .append(CsvUtil.escape(e.getCorporateEmail())).append(",")
                .append(CsvUtil.escape(e.getPhone())).append(",")
                .append(CsvUtil.escape(e.getStatusId() != null ? statusMap.get(e.getStatusId()) : "")).append(",")
                .append(e.getActive()).append(",")
                .append(CsvUtil.formatDate(e.getCreatedAt())).append("\n"));

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ─── Helpers service ──────────────────────────────────────────────────────────────

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

}
