package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.FileMetadataResponse;
import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.common.service.StorageService;
import com.crm.mcsv_rrhh.dto.ContractAnnexRequest;
import com.crm.mcsv_rrhh.dto.ContractAnnexResponse;
import com.crm.mcsv_rrhh.dto.UpdateContractAnnexRequest;
import com.crm.mcsv_rrhh.entity.ContractAnnex;
import com.crm.mcsv_rrhh.entity.ContractAnnexType;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.repository.*;
import com.crm.mcsv_rrhh.service.ContractAnnexService;
import com.crm.mcsv_rrhh.service.HRRequestService;
import com.crm.mcsv_rrhh.util.FileUploadHelper;
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
public class ContractAnnexServiceImpl implements ContractAnnexService {

    private static final String ENTITY_TYPE = "ANNEX";
    private static final int MAX_DOCUMENTS = 5;
    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    private final ContractAnnexRepository repository;
    private final ContractAnnexTypeRepository annexTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final HRRequestRepository hrRequestRepository;
    private final HRRequestService hrRequestService;
    private final StorageService storageService;
    private final FileUploadHelper fileUploadHelper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContractAnnexResponse> list(String search, String status,
                                                      Long annexTypeId, Long contractId,
                                                      LocalDate dateFrom, LocalDate dateTo,
                                                      LocalDate effectiveDateFrom, LocalDate effectiveDateTo,
                                                      LocalDate createdFrom, LocalDate createdTo,
                                                      LocalDate updatedFrom, LocalDate updatedTo,
                                                      Pageable pageable, String sortBy, String sortDir) {
        Long statusId = status != null && !status.isBlank()
                ? employeeStatusRepository.findByName(status).map(s -> s.getId()).orElse(null)
                : null;

        Pageable effectivePageable = (sortBy != null && EMPLOYEE_SORT_FIELDS.contains(sortBy))
                ? org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
                : pageable;

        Page<ContractAnnex> page = repository.findAll(
                ContractAnnexSpecification.withFilters(search, statusId, annexTypeId, contractId,
                        dateFrom, dateTo, effectiveDateFrom, effectiveDateTo,
                        createdFrom, createdTo, updatedFrom, updatedTo, sortBy, sortDir),
                effectivePageable);

        long total = page.getTotalElements();
        long approved = resolveApprovedCount();

        return PagedResponse.of(page.map(e -> {
            Optional<HRRequest> hrReq = hrRequestRepository.findTopByAnnexIdOrderByCreatedAtDesc(e.getId());
            Long reqId = hrReq.map(HRRequest::getId).orElse(null);
            String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
            return toResponse(e, Collections.emptyList(), reqId, statusName);
        }), total, approved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractAnnexResponse getById(Long id) {
        ContractAnnex entity = findOrThrow(id);
        Optional<HRRequest> hrReq = hrRequestRepository.findTopByAnnexIdOrderByCreatedAtDesc(id);
        Long requestId = hrReq.map(HRRequest::getId).orElse(null);
        String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
        return toResponse(entity, fetchDocuments(id), requestId, statusName);
    }

    @Override
    @Transactional
    public ContractAnnexResponse create(ContractAnnexRequest request, List<MultipartFile> files) {
        ContractAnnex entity = ContractAnnex.builder()
                .employeeId(request.getEmployeeId())
                .contractId(request.getContractId())
                .annexTypeId(request.getAnnexTypeId())
                .date(request.getDate())
                .effectiveDate(request.getEffectiveDate())
                .description(request.getDescription())
                .build();

        ContractAnnex saved = repository.save(entity);
        HRRequest hrReq = hrRequestService.createForAnnex(saved.getId(), saved.getEmployeeId(), "CREATE", null);

        List<FileMetadataResponse> documents = uploadFiles(saved.getId(), saved.getEmployeeId(), files);
        String statusName = resolveStatusName(hrReq.getStatusId());
        return toResponse(saved, documents, hrReq.getId(), statusName);
    }

    @Override
    @Transactional
    public ContractAnnexResponse update(UpdateContractAnnexRequest request, List<MultipartFile> files) {
        ContractAnnex entity = findOrThrow(request.getId());

        String proposedData;
        try {
            proposedData = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando proposedData", e);
        }

        HRRequest hrReq = hrRequestService.createForAnnex(entity.getId(), entity.getEmployeeId(), "UPDATE", proposedData);

        uploadPendingFiles(hrReq.getId(), entity.getEmployeeId(), files);

        Long requestId = hrRequestRepository.findTopByAnnexIdOrderByCreatedAtDesc(entity.getId())
                .map(HRRequest::getId).orElse(hrReq.getId());

        List<FileMetadataResponse> documents = fetchDocuments(entity.getId());
        String statusName = resolveStatusName(hrReq.getStatusId());
        return toResponse(entity, documents, requestId, statusName);
    }

    @Override
    public void deleteDocument(Long annexId, Long fileId, Long userId) {
        if (!repository.existsById(annexId)) {
            throw new ResourceNotFoundException("Anexo no encontrado: " + annexId);
        }
        storageService.delete(fileId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Empleado,RUT,Contrato,Tipo Anexo,Fecha,Fecha Vigencia,Descripción,Estado,Fecha Creación\n");

        repository.findAll().forEach(e -> {
            String statusName = hrRequestRepository.findTopByAnnexIdOrderByCreatedAtDesc(e.getId())
                    .map(r -> resolveStatusName(r.getStatusId())).orElse("");
            String annexTypeName = e.getAnnexType() != null ? e.getAnnexType().getName() : "";

            csv.append(e.getId()).append(",")
               .append(escape(fullName(e.getEmployee()))).append(",")
               .append(e.getEmployee() != null ? e.getEmployee().getIdentification() : "").append(",")
               .append(e.getContractId()).append(",")
               .append(escape(annexTypeName)).append(",")
               .append(e.getDate() != null ? e.getDate() : "").append(",")
               .append(e.getEffectiveDate() != null ? e.getEffectiveDate() : "").append(",")
               .append(escape(e.getDescription())).append(",")
               .append(escape(statusName)).append(",")
               .append(formatDate(e.getCreatedAt())).append("\n");
        });

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractAnnexResponse> findByContract(Long contractId) {
        return repository.findByContractId(contractId).stream()
                .map(e -> {
                    Optional<HRRequest> hrReq = hrRequestRepository.findTopByAnnexIdOrderByCreatedAtDesc(e.getId());
                    Long reqId = hrReq.map(HRRequest::getId).orElse(null);
                    String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
                    return toResponse(e, Collections.emptyList(), reqId, statusName);
                })
                .toList();
    }

    // ─── File helpers ─────────────────────────────────────────────────────────

    private List<FileMetadataResponse> uploadFiles(Long annexId, Long uploadedBy, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return Collections.emptyList();

        List<FileMetadataResponse> existing = fetchDocuments(annexId);
        if (existing.size() + files.size() > MAX_DOCUMENTS) {
            throw new IllegalArgumentException(
                    "El anexo ya tiene " + existing.size() + " documento(s). " +
                    "Máximo permitido: " + MAX_DOCUMENTS + ".");
        }
        return fileUploadHelper.uploadFiles(files, uploadedBy, ENTITY_TYPE, annexId);
    }

    private void uploadPendingFiles(Long hrRequestId, Long uploadedBy, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            fileUploadHelper.validateFile(file);
            storageService.upload(file, uploadedBy, "ANNEX_PENDING", hrRequestId, false);
        }
    }

    private List<FileMetadataResponse> fetchDocuments(Long annexId) {
        try {
            List<FileMetadataResponse> response = storageService.listByEntity(ENTITY_TYPE, annexId);
            return response != null ? response : Collections.emptyList();
        } catch (Exception e) {
            log.warn("No se pudieron obtener documentos del anexo {}: {}", annexId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ContractAnnex findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anexo no encontrado con id: " + id));
    }

    private ContractAnnexResponse toResponse(ContractAnnex e, List<FileMetadataResponse> documents,
                                              Long requestId, String statusName) {
        Employee emp = e.getEmployee();
        ContractAnnexType annexType = e.getAnnexType();
        return ContractAnnexResponse.builder()
                .id(e.getId())
                .status(statusName)
                .employeeId(e.getEmployeeId())
                .employeeFullName(fullName(emp))
                .employeeIdentification(emp != null ? emp.getIdentification() : null)
                .contractId(e.getContractId())
                .annexTypeId(e.getAnnexTypeId())
                .annexTypeName(annexType != null ? annexType.getName() : null)
                .requireApproval(annexType != null ? annexType.getRequireApproval() : null)
                .date(e.getDate())
                .effectiveDate(e.getEffectiveDate())
                .description(e.getDescription())
                .documents(documents)
                .hrRequestId(requestId)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private String resolveStatusName(Long statusId) {
        if (statusId == null) return null;
        return employeeStatusRepository.findById(statusId).map(s -> s.getName()).orElse(null);
    }

    private long resolveApprovedCount() {
        return employeeStatusRepository.findByName("Aprobado")
                .map(s -> hrRequestRepository.findAll().stream()
                        .filter(r -> r.getAnnexId() != null && s.getId().equals(r.getStatusId()))
                        .map(HRRequest::getAnnexId)
                        .distinct()
                        .count())
                .orElse(0L);
    }

    private String fullName(Employee emp) {
        if (emp == null) return null;
        return String.join(" ", emp.getFirstName(),
                emp.getPaternalLastName() != null ? emp.getPaternalLastName() : "",
                emp.getMaternalLastName() != null ? emp.getMaternalLastName() : "").trim();
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }

    private String formatDate(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
