package com.crm.mcsv_rrhh.service.impl;

import com.crm.mcsv_rrhh.client.StorageClient;
import com.crm.mcsv_rrhh.dto.BulkImportResult;
import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.ContractDetailResponse;
import com.crm.mcsv_rrhh.dto.ContractResponse;
import com.crm.mcsv_rrhh.dto.CreateContractRequest;
import com.crm.mcsv_rrhh.dto.FileMetadataResponse;
import com.crm.mcsv_rrhh.dto.UpdateContractRequest;
import com.crm.mcsv_rrhh.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.crm.mcsv_rrhh.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.*;
import com.crm.mcsv_rrhh.repository.ContractSpecification;
import com.crm.mcsv_rrhh.service.ContractService;
import com.crm.mcsv_rrhh.service.HRRequestService;
import com.crm.mcsv_rrhh.util.FileUploadHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private static final String ENTITY_TYPE = "CONTRACT";
    private static final int MAX_DOCUMENTS = 5;

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final HRRequestRepository hrRequestRepository;
    private final HRRequestService hrRequestService;
    private final ObjectMapper objectMapper;
    private final StorageClient storageClient;
    private final FileUploadHelper fileUploadHelper;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final SafetyGroupRepository safetyGroupRepository;
    private final CompanyRepository companyRepository;
    private final ZoneRepository zoneRepository;
    private final JobTitleRepository jobTitleRepository;
    private final SiteRepository siteRepository;
    private final LaborUnionRepository laborUnionRepository;
    private final MealTypeRepository mealTypeRepository;
    private final TransportTypeRepository transportTypeRepository;

    @Override
    @Transactional
    public ContractDetailResponse createContract(CreateContractRequest request, List<MultipartFile> files) {
        Long pendingStatusId = employeeStatusRepository.findByName("Pendiente de revisión")
                .map(EmployeeStatus::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: Pendiente de revisión"));

        Long suspendedContractStatusId = contractStatusRepository.findByName("Suspendido")
                .map(ContractStatus::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de contrato no encontrado: Suspendido"));

        Long contractTypeId = request.getEndDate() == null
                ? contractTypeRepository.findByName("Indefinido")
                        .map(ContractType::getId)
                        .orElse(request.getContractTypeId())
                : request.getContractTypeId();

        Contract contract = Contract.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .contractNumber(request.getContractNumber())
                .contractTypeId(contractTypeId)
                .contractStatusId(suspendedContractStatusId)
                .safetyGroupId(request.getSafetyGroupId())
                .contractDetail(request.getContractDetail())
                .baseSalary(request.getBaseSalary())
                .agreedSalary(request.getAgreedSalary())
                .companyId(request.getCompanyId())
                .zoneId(request.getZoneId())
                .jobTitleId(request.getJobTitleId())
                .siteId(request.getSiteId())
                .laborUnionId(request.getLaborUnionId())
                .weeklyWorkHours(request.getWeeklyWorkHours())
                .workDays(request.getWorkDays())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .mealTypeId(request.getMealTypeId())
                .transportTypeId(request.getTransportTypeId())
                .statusId(pendingStatusId)
                .build();

        Contract saved = contractRepository.save(contract);
        HRRequest req = hrRequestService.createForContract(saved.getId(), saved.getEmployeeId(), "CREATE", null);

        List<FileMetadataResponse> documents = uploadFiles(saved.getId(), saved.getEmployeeId(), files);
        return toDetailResponse(saved, req.getId(), documents);
    }

    // ─── Detalle ──────────────────────────────────────────────────────────────

    @Override
    public ContractDetailResponse getById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));
        Long requestId = hrRequestRepository.findTopByContractIdOrderByCreatedAtDesc(id)
                .map(HRRequest::getId).orElse(null);
        List<FileMetadataResponse> documents = fetchDocuments(id);
        return toDetailResponse(contract, requestId, documents);
    }

    // ─── Editar ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ContractDetailResponse updateContract(Long id, UpdateContractRequest req, List<MultipartFile> files) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));

        String proposedData;
        try {
            proposedData = objectMapper.writeValueAsString(req);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando proposedData", e);
        }

        HRRequest hrReq = hrRequestService.createForContract(id, contract.getEmployeeId(), "UPDATE", proposedData);

        // Archivos pendientes de aprobación: se suben con CONTRACT_PENDING + hrRequestId
        uploadPendingFiles(hrReq.getId(), contract.getEmployeeId(), files);

        Long requestId = hrRequestRepository.findTopByContractIdOrderByCreatedAtDesc(id)
                .map(HRRequest::getId).orElse(hrReq.getId());

        List<FileMetadataResponse> documents = fetchDocuments(id);
        return toDetailResponse(contract, requestId, documents);
    }

    // ─── Listar ───────────────────────────────────────────────────────────────

    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    @Override
    public Page<ContractResponse> list(String search,
                                       Long employeeId, Long statusId,
                                       Long contractStatusId, Long contractTypeId,
                                       LocalDate createdFrom, LocalDate createdTo,
                                       LocalDate startDateFrom, LocalDate startDateTo,
                                       LocalDate endDateFrom, LocalDate endDateTo,
                                       LocalDate updatedFrom, LocalDate updatedTo,
                                       Pageable pageable, String sortBy, String sortDir) {
        Pageable effectivePageable = (sortBy != null && EMPLOYEE_SORT_FIELDS.contains(sortBy))
                ? org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
                : pageable;
        Specification<Contract> spec = ContractSpecification.withFilters(search, employeeId, statusId, contractStatusId, contractTypeId, createdFrom, createdTo, startDateFrom, startDateTo, endDateFrom, endDateTo, updatedFrom, updatedTo, sortBy, sortDir);
        return contractRepository.findAll(spec, effectivePageable).map(this::toResponse);
    }

    @Override
    public Map<String, Long> getStats(Long employeeId) {
        Long pendingStatusId = contractStatusRepository.findByName("Pendiente de revisión")
                .map(ContractStatus::getId).orElse(-1L);
        Long activeContractStatusId = contractStatusRepository.findByName("Activo")
                .map(ContractStatus::getId).orElse(-1L);

        long total   = employeeId != null ? contractRepository.countByEmployeeId(employeeId) : contractRepository.count();
        long active  = employeeId != null ? contractRepository.countByEmployeeIdAndContractStatusId(employeeId, activeContractStatusId) : contractRepository.countByContractStatusId(activeContractStatusId);
        long pending = employeeId != null ? contractRepository.countByEmployeeIdAndStatusId(employeeId, pendingStatusId) : contractRepository.countByStatusId(pendingStatusId);

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("pending", pending);
        return stats;
    }

    // ─── Documentos ───────────────────────────────────────────────────────────

    @Override
    public void deleteDocument(Long contractId, Long fileId, Long userId) {
        if (!contractRepository.existsById(contractId)) {
            throw new ResourceNotFoundException("Contrato no encontrado: " + contractId);
        }
        storageClient.delete(fileId, userId);
    }

    private void uploadPendingFiles(Long hrRequestId, Long uploadedBy, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            fileUploadHelper.validateFile(file);
            storageClient.upload(file, uploadedBy, "CONTRACT_PENDING", hrRequestId, false);
        }
    }

    private List<FileMetadataResponse> uploadFiles(Long contractId, Long uploadedBy, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return Collections.emptyList();

        List<FileMetadataResponse> existing = fetchDocuments(contractId);
        if (existing.size() + files.size() > MAX_DOCUMENTS) {
            throw new IllegalArgumentException(
                    "El contrato ya tiene " + existing.size() + " documento(s). " +
                    "Máximo permitido: " + MAX_DOCUMENTS + ".");
        }

        return fileUploadHelper.uploadFiles(files, uploadedBy, ENTITY_TYPE, contractId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ContractResponse toResponse(Contract c) {
        var employee = employeeRepository.findById(c.getEmployeeId()).orElse(null);
        String employeeName = employee != null ? employee.getFirstName() + " " + employee.getPaternalLastName() : null;
        String employeeIdentification = employee != null ? employee.getIdentification() : null;

        return ContractResponse.builder()
                .id(c.getId())
                .employeeId(c.getEmployeeId())
                .employeeName(employeeName)
                .employeeIdentification(employeeIdentification)
                .name(c.getName())
                .contractNumber(c.getContractNumber())
                .contractType(resolveName(c.getContractTypeId(), contractTypeRepository))
                .contractStatus(resolveName(c.getContractStatusId(), contractStatusRepository))
                .company(resolveName(c.getCompanyId(), companyRepository))
                .jobTitle(resolveName(c.getJobTitleId(), jobTitleRepository))
                .baseSalary(c.getBaseSalary())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private List<FileMetadataResponse> fetchDocuments(Long contractId) {
        try {
            ResponseEntity<List<FileMetadataResponse>> response =
                    storageClient.listByEntity(ENTITY_TYPE, contractId);
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("No se pudieron obtener documentos del contrato {}: {}", contractId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private ContractDetailResponse toDetailResponse(Contract c, Long requestId, List<FileMetadataResponse> documents) {
        var employee = employeeRepository.findById(c.getEmployeeId()).orElse(null);
        String employeeName = employee != null ? employee.getFirstName() + " " + employee.getPaternalLastName() : null;
        String employeeIdentification = employee != null ? employee.getIdentification() : null;

        return ContractDetailResponse.builder()
                .id(c.getId())
                .employeeId(c.getEmployeeId())
                .employeeName(employeeName)
                .employeeIdentification(employeeIdentification)
                .name(c.getName())
                .contractNumber(c.getContractNumber())
                .contractType(resolve(c.getContractTypeId(), contractTypeRepository))
                .contractStatus(resolve(c.getContractStatusId(), contractStatusRepository))
                .safetyGroup(resolve(c.getSafetyGroupId(), safetyGroupRepository))
                .contractDetail(c.getContractDetail())
                .baseSalary(c.getBaseSalary())
                .agreedSalary(c.getAgreedSalary())
                .company(resolve(c.getCompanyId(), companyRepository))
                .zone(resolve(c.getZoneId(), zoneRepository))
                .jobTitle(resolve(c.getJobTitleId(), jobTitleRepository))
                .site(resolve(c.getSiteId(), siteRepository))
                .laborUnion(resolve(c.getLaborUnionId(), laborUnionRepository))
                .weeklyWorkHours(c.getWeeklyWorkHours())
                .workDays(c.getWorkDays())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .mealType(resolve(c.getMealTypeId(), mealTypeRepository))
                .transportType(resolve(c.getTransportTypeId(), transportTypeRepository))
                .status(resolve(c.getStatusId(), employeeStatusRepository))
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .requestId(requestId)
                .documents(documents)
                .build();
    }

    private <T> String resolveName(Long id, JpaRepository<T, Long> repo) {
        if (id == null) return null;
        return repo.findById(id).map(e -> {
            try { return (String) e.getClass().getMethod("getName").invoke(e); }
            catch (Exception ex) { return null; }
        }).orElse(null);
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

    // ─── Export CSV ───────────────────────────────────────────────────────────

    @Override
    public byte[] exportCsv() {
        Map<Long, String> contractStatusMap = contractStatusRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(ContractStatus::getId, ContractStatus::getName));
        Map<Long, String> contractTypeMap = contractTypeRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(ContractType::getId, ContractType::getName));
        Map<Long, String> companyMap = companyRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> jobTitleMap = jobTitleRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(JobTitle::getId, JobTitle::getName));
        Map<Long, Employee> employeeMap = employeeRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Employee::getId, e -> e));

        StringBuilder csv = new StringBuilder();
        csv.append("ID,RUT Trabajador,Nombre Trabajador,Nombre Contrato,Número Contrato,Tipo Contrato,Estado Contrato,Empresa,Cargo,Salario Base,Fecha Inicio,Fecha Fin,Fecha Creación,Fecha Actualización\n");

        contractRepository.findAll().forEach(c -> {
            Employee emp = employeeMap.get(c.getEmployeeId());
            csv.append(c.getId()).append(",")
               .append(escape(emp != null ? emp.getIdentification() : "")).append(",")
               .append(escape(emp != null ? emp.getFirstName() + " " + emp.getPaternalLastName() : "")).append(",")
               .append(escape(c.getName())).append(",")
               .append(escape(c.getContractNumber())).append(",")
               .append(escape(contractTypeMap.get(c.getContractTypeId()))).append(",")
               .append(escape(contractStatusMap.get(c.getContractStatusId()))).append(",")
               .append(escape(companyMap.get(c.getCompanyId()))).append(",")
               .append(escape(jobTitleMap.get(c.getJobTitleId()))).append(",")
               .append(escape(c.getBaseSalary())).append(",")
               .append(formatDate(c.getStartDate())).append(",")
               .append(formatDate(c.getEndDate())).append(",")
               .append(formatDateTime(c.getCreatedAt())).append(",")
               .append(formatDateTime(c.getUpdatedAt())).append("\n");
        });

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ─── Import CSV ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public BulkImportResult importFromCsv(MultipartFile file) {
        List<BulkImportResult.RowError> errors = new ArrayList<>();
        int total = 0;
        int success = 0;

        Long pendingStatusId = employeeStatusRepository.findByName("Pendiente de revisión")
                .map(EmployeeStatus::getId).orElse(null);
        Long suspendedContractStatusId = contractStatusRepository.findByName("Suspendido")
                .map(ContractStatus::getId).orElse(null);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return BulkImportResult.builder().total(0).success(0).failed(0).errors(errors).build();
            }
            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> idx = buildHeaderIndex(headers);

            int iRut            = idx.getOrDefault("rut trabajador", -1);
            int iName           = idx.getOrDefault("nombre contrato", -1);
            int iContractNumber = idx.getOrDefault("número contrato", idx.getOrDefault("numero contrato", -1));
            int iContractType   = idx.getOrDefault("tipo contrato", -1);
            int iBaseSalary     = idx.getOrDefault("salario base", -1);
            int iStartDate      = idx.getOrDefault("fecha inicio", -1);
            int iEndDate        = idx.getOrDefault("fecha fin", -1);

            if (iRut < 0 || iName < 0) {
                errors.add(new BulkImportResult.RowError(1, "Faltan columnas requeridas: 'RUT Trabajador' y 'Nombre Contrato'"));
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

                    String rut = col(cols, iRut);
                    Employee employee = employeeRepository.findByIdentification(rut)
                            .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con RUT: " + rut));

                    Long contractTypeId = null;
                    String contractTypeName = col(cols, iContractType);
                    if (!contractTypeName.isBlank()) {
                        contractTypeId = contractTypeRepository.findByName(contractTypeName)
                                .map(ContractType::getId)
                                .orElseThrow(() -> new IllegalArgumentException("Tipo de contrato no encontrado: " + contractTypeName));
                    }

                    LocalDate startDate = parseDate(col(cols, iStartDate));
                    LocalDate endDate   = parseDate(col(cols, iEndDate));

                    if (contractTypeId == null) {
                        contractTypeId = endDate == null
                                ? contractTypeRepository.findByName("Indefinido").map(ContractType::getId).orElse(null)
                                : null;
                    }

                    Contract contract = Contract.builder()
                            .employeeId(employee.getId())
                            .name(col(cols, iName))
                            .contractNumber(col(cols, iContractNumber).isEmpty() ? null : col(cols, iContractNumber))
                            .contractTypeId(contractTypeId)
                            .contractStatusId(suspendedContractStatusId)
                            .baseSalary(col(cols, iBaseSalary).isEmpty() ? null : col(cols, iBaseSalary))
                            .startDate(startDate)
                            .endDate(endDate)
                            .statusId(pendingStatusId)
                            .build();

                    Contract saved = contractRepository.save(contract);
                    hrRequestService.createForContract(saved.getId(), saved.getEmployeeId(), "CREATE", null);
                    success++;
                } catch (Exception e) {
                    errors.add(new BulkImportResult.RowError(row, e.getMessage()));
                }
            }
        } catch (Exception e) {
            log.error("Error reading CSV file for contracts", e);
            errors.add(new BulkImportResult.RowError(0, "Error leyendo el archivo: " + e.getMessage()));
        }

        return BulkImportResult.builder()
                .total(total)
                .success(success)
                .failed(errors.size())
                .errors(errors)
                .build();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Fecha inválida: " + value + ". Use dd-MM-yyyy o yyyy-MM-dd");
            }
        }
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
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            idx.put(headers[i].trim().toLowerCase(), i);
        }
        return idx;
    }

    private String col(String[] cols, int index) {
        if (index < 0 || index >= cols.length) return "";
        return cols[index].trim();
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private String formatDateTime(java.time.LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }
}
