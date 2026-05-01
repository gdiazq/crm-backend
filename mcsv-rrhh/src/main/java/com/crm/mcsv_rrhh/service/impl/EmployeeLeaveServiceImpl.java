package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.FileMetadataResponse;
import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.common.service.StorageService;
import com.crm.mcsv_rrhh.dto.EmployeeLeaveRequest;
import com.crm.mcsv_rrhh.dto.EmployeeLeaveResponse;
import com.crm.mcsv_rrhh.dto.UpdateEmployeeLeaveRequest;
import com.crm.mcsv_rrhh.entity.Contract;
import com.crm.mcsv_rrhh.entity.ContractStatus;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.EmployeeLeave;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.entity.LeaveType;
import com.crm.mcsv_rrhh.repository.ContractRepository;
import com.crm.mcsv_rrhh.repository.ContractStatusRepository;
import com.crm.mcsv_rrhh.repository.EmployeeLeaveRepository;
import com.crm.mcsv_rrhh.repository.EmployeeLeaveSpecification;
import com.crm.mcsv_rrhh.repository.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.HRRequestRepository;
import com.crm.mcsv_rrhh.service.EmployeeLeaveService;
import com.crm.mcsv_rrhh.service.HRRequestService;
import com.crm.mcsv_rrhh.util.FileUploadHelper;
import com.crm.mcsv_rrhh.util.LeaveCalculator;
import com.crm.mcsv_rrhh.util.LeaveValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeLeaveServiceImpl implements EmployeeLeaveService {

    private static final String ENTITY_TYPE = "LEAVE";
    private static final String PENDING_ENTITY_TYPE = "LEAVE_PENDING";
    private static final int MAX_DOCUMENTS = 5;
    private static final String ACTIVE_CONTRACT_STATUS = "Activo";
    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    private final EmployeeLeaveRepository repository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final HRRequestRepository hrRequestRepository;
    private final HRRequestService hrRequestService;
    private final StorageService storageService;
    private final FileUploadHelper fileUploadHelper;
    private final LeaveValidator leaveValidator;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeLeaveResponse> list(String search, String status,
                                                     Long leaveTypeId, Long employeeId, Long contractId,
                                                     LocalDate startFrom, LocalDate startTo,
                                                     LocalDate createdFrom, LocalDate createdTo,
                                                     LocalDate updatedFrom, LocalDate updatedTo,
                                                     Pageable pageable, String sortBy, String sortDir) {
        Long statusId = status != null && !status.isBlank()
                ? employeeStatusRepository.findByName(status).map(s -> s.getId()).orElse(null)
                : null;

        Pageable effectivePageable = (sortBy != null && EMPLOYEE_SORT_FIELDS.contains(sortBy))
                ? org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
                : pageable;

        Page<EmployeeLeave> page = repository.findAll(
                EmployeeLeaveSpecification.withFilters(search, statusId, leaveTypeId, employeeId, contractId,
                        startFrom, startTo, createdFrom, createdTo, updatedFrom, updatedTo, sortBy, sortDir),
                effectivePageable);

        long total = page.getTotalElements();
        long approved = resolveApprovedCount();

        return PagedResponse.of(page.map(e -> {
            Optional<HRRequest> hrReq = hrRequestRepository.findTopByLeaveIdOrderByCreatedAtDesc(e.getId());
            Long requestId = hrReq.map(HRRequest::getId).orElse(null);
            String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
            return toResponse(e, Collections.emptyList(), requestId, statusName);
        }), total, approved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeLeaveResponse getById(Long id) {
        EmployeeLeave entity = findOrThrow(id);
        Optional<HRRequest> hrReq = hrRequestRepository.findTopByLeaveIdOrderByCreatedAtDesc(id);
        Long requestId = hrReq.map(HRRequest::getId).orElse(null);
        String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
        return toResponse(entity, fetchDocuments(id), requestId, statusName);
    }

    @Override
    @Transactional
    public EmployeeLeaveResponse create(EmployeeLeaveRequest request, List<MultipartFile> files) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + request.getEmployeeId()));
        Long contractId = resolveActiveContractId(employee.getId());

        EmployeeLeave candidate = EmployeeLeave.builder()
                .employeeId(employee.getId())
                .contractId(contractId)
                .leaveTypeId(request.getLeaveTypeId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .halfDay(Boolean.TRUE.equals(request.getHalfDay()))
                .totalDays(LeaveCalculator.computeTotalDays(request.getStartDate(), request.getEndDate(), request.getHalfDay()))
                .reason(request.getReason())
                .build();

        leaveValidator.validate(candidate, files, null, null);

        EmployeeLeave saved = repository.save(candidate);
        HRRequest hrReq = hrRequestService.createForLeave(saved.getId(), saved.getEmployeeId(), "CREATE", null);

        List<FileMetadataResponse> documents = uploadFiles(saved.getId(), saved.getEmployeeId(), files);
        String statusName = resolveStatusName(hrReq.getStatusId());
        return toResponse(saved, documents, hrReq.getId(), statusName);
    }

    @Override
    @Transactional
    public EmployeeLeaveResponse update(UpdateEmployeeLeaveRequest request, List<MultipartFile> files) {
        EmployeeLeave entity = findOrThrow(request.getId());
        EmployeeLeave candidate = merge(entity, request);

        leaveValidator.validate(candidate, files, entity.getId(), null);

        String proposedData;
        try {
            proposedData = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando proposedData", e);
        }

        HRRequest hrReq = hrRequestService.createForLeave(entity.getId(), entity.getEmployeeId(), "UPDATE", proposedData);
        uploadPendingFiles(entity.getId(), hrReq.getId(), entity.getEmployeeId(), files);

        Long requestId = hrRequestRepository.findTopByLeaveIdOrderByCreatedAtDesc(entity.getId())
                .map(HRRequest::getId).orElse(hrReq.getId());

        List<FileMetadataResponse> documents = fetchDocuments(entity.getId());
        String statusName = resolveStatusName(hrReq.getStatusId());
        return toResponse(entity, documents, requestId, statusName);
    }

    @Override
    @Transactional
    public void deleteDocument(Long leaveId, Long fileId, Long userId) {
        if (!repository.existsById(leaveId)) {
            throw new ResourceNotFoundException("Permiso no encontrado: " + leaveId);
        }
        storageService.delete(fileId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Empleado,RUT,Contrato,Tipo Permiso,Fecha Inicio,Fecha Término,Medio Día,Días Totales,Motivo,Estado,Fecha Creación\n");

        repository.findAll().forEach(e -> {
            String statusName = hrRequestRepository.findTopByLeaveIdOrderByCreatedAtDesc(e.getId())
                    .map(r -> resolveStatusName(r.getStatusId())).orElse("");
            String leaveTypeName = e.getLeaveType() != null ? e.getLeaveType().getName() : "";

            csv.append(e.getId()).append(",")
                    .append(escape(fullName(e.getEmployee()))).append(",")
                    .append(e.getEmployee() != null ? e.getEmployee().getIdentification() : "").append(",")
                    .append(e.getContractId()).append(",")
                    .append(escape(leaveTypeName)).append(",")
                    .append(e.getStartDate() != null ? e.getStartDate() : "").append(",")
                    .append(e.getEndDate() != null ? e.getEndDate() : "").append(",")
                    .append(e.getHalfDay()).append(",")
                    .append(e.getTotalDays() != null ? e.getTotalDays() : "").append(",")
                    .append(escape(e.getReason())).append(",")
                    .append(escape(statusName)).append(",")
                    .append(formatDate(e.getCreatedAt())).append("\n");
        });

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLeaveResponse> findByEmployee(Long employeeId) {
        return repository.findByEmployeeId(employeeId).stream()
                .map(e -> {
                    Optional<HRRequest> hrReq = hrRequestRepository.findTopByLeaveIdOrderByCreatedAtDesc(e.getId());
                    Long requestId = hrReq.map(HRRequest::getId).orElse(null);
                    String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
                    return toResponse(e, Collections.emptyList(), requestId, statusName);
                })
                .toList();
    }

    private List<FileMetadataResponse> uploadFiles(Long leaveId, Long uploadedBy, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        if (files.size() > MAX_DOCUMENTS) {
            throw new IllegalArgumentException(
                    "El permiso supera el máximo permitido de " + MAX_DOCUMENTS + " documento(s).");
        }
        return fileUploadHelper.uploadFiles(files, uploadedBy, ENTITY_TYPE, leaveId);
    }

    private void uploadPendingFiles(Long leaveId, Long hrRequestId, Long uploadedBy, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        List<FileMetadataResponse> existing = fetchDocuments(leaveId);
        if (existing.size() + files.size() > MAX_DOCUMENTS) {
            throw new IllegalArgumentException(
                    "El permiso ya tiene " + existing.size() + " documento(s). " +
                            "Máximo permitido: " + MAX_DOCUMENTS + ".");
        }

        for (MultipartFile file : files) {
            fileUploadHelper.validateFile(file);
            storageService.upload(file, uploadedBy, PENDING_ENTITY_TYPE, hrRequestId, false);
        }
    }

    private List<FileMetadataResponse> fetchDocuments(Long leaveId) {
        try {
            List<FileMetadataResponse> response = storageService.listByEntity(ENTITY_TYPE, leaveId);
            return response != null ? response : Collections.emptyList();
        } catch (Exception e) {
            log.warn("No se pudieron obtener documentos del permiso {}: {}", leaveId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private EmployeeLeave findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + id));
    }

    private Long resolveActiveContractId(Long employeeId) {
        Long activeContractStatusId = contractStatusRepository.findByName(ACTIVE_CONTRACT_STATUS)
                .map(ContractStatus::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de contrato no encontrado: " + ACTIVE_CONTRACT_STATUS));

        List<Contract> activeContracts = contractRepository.findByEmployeeId(employeeId).stream()
                .filter(contract -> activeContractStatusId.equals(contract.getContractStatusId()))
                .toList();

        if (activeContracts.isEmpty()) {
            throw new IllegalStateException("El empleado no tiene un contrato activo");
        }
        if (activeContracts.size() > 1) {
            throw new IllegalStateException("El empleado tiene más de un contrato activo");
        }
        return activeContracts.getFirst().getId();
    }

    private EmployeeLeave merge(EmployeeLeave current, UpdateEmployeeLeaveRequest request) {
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : current.getStartDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : current.getEndDate();
        Boolean halfDay = request.getHalfDay() != null ? request.getHalfDay() : current.getHalfDay();

        return EmployeeLeave.builder()
                .id(current.getId())
                .employeeId(current.getEmployeeId())
                .contractId(current.getContractId())
                .leaveTypeId(request.getLeaveTypeId() != null ? request.getLeaveTypeId() : current.getLeaveTypeId())
                .startDate(startDate)
                .endDate(endDate)
                .halfDay(Boolean.TRUE.equals(halfDay))
                .totalDays(LeaveCalculator.computeTotalDays(startDate, endDate, halfDay))
                .reason(request.getReason() != null ? request.getReason() : current.getReason())
                .createdAt(current.getCreatedAt())
                .updatedAt(current.getUpdatedAt())
                .build();
    }

    private EmployeeLeaveResponse toResponse(EmployeeLeave e, List<FileMetadataResponse> documents,
                                             Long requestId, String statusName) {
        Employee employee = e.getEmployee();
        LeaveType leaveType = e.getLeaveType();

        return EmployeeLeaveResponse.builder()
                .id(e.getId())
                .status(statusName)
                .employeeId(e.getEmployeeId())
                .employeeFullName(fullName(employee))
                .employeeIdentification(employee != null ? employee.getIdentification() : null)
                .contractId(e.getContractId())
                .leaveTypeId(e.getLeaveTypeId())
                .leaveTypeName(leaveType != null ? leaveType.getName() : null)
                .paid(leaveType != null ? leaveType.getPaid() : null)
                .requiresDocument(leaveType != null ? leaveType.getRequiresDocument() : null)
                .requireApproval(leaveType != null ? leaveType.getRequireApproval() : null)
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .halfDay(e.getHalfDay())
                .totalDays(e.getTotalDays())
                .reason(e.getReason())
                .documents(documents)
                .hrRequestId(requestId)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private String resolveStatusName(Long statusId) {
        if (statusId == null) {
            return null;
        }
        return employeeStatusRepository.findById(statusId).map(s -> s.getName()).orElse(null);
    }

    private long resolveApprovedCount() {
        return employeeStatusRepository.findByName("Aprobado")
                .map(s -> hrRequestRepository.countLeavesWithLatestStatusId(s.getId()))
                .orElse(0L);
    }

    private String fullName(Employee employee) {
        if (employee == null) {
            return null;
        }
        return String.join(" ", employee.getFirstName(),
                employee.getPaternalLastName() != null ? employee.getPaternalLastName() : "",
                employee.getMaternalLastName() != null ? employee.getMaternalLastName() : "").trim();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
