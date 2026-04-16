package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.FileMetadataResponse;
import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.common.storage.service.StorageService;
import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.dto.TransferRequest;
import com.crm.mcsv_rrhh.dto.TransferResponse;
import com.crm.mcsv_rrhh.dto.UpdateTransferRequest;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.entity.Transfer;
import com.crm.mcsv_rrhh.repository.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.HRRequestRepository;
import com.crm.mcsv_rrhh.repository.TransferRepository;
import com.crm.mcsv_rrhh.service.HRRequestService;
import com.crm.mcsv_rrhh.service.TransferService;
import com.crm.mcsv_rrhh.util.FileUploadHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private static final String ENTITY_TYPE = "TRANSFER";
    private static final int MAX_DOCUMENTS = 5;

    private final TransferRepository repository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final HRRequestRepository hrRequestRepository;
    private final HRRequestService hrRequestService;
    private final ProjectClient projectClient;
    private final StorageService storageService;
    private final FileUploadHelper fileUploadHelper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TransferResponse> list(Long employeeId, String status, Pageable pageable) {
        Long statusId = status != null && !status.isBlank()
                ? employeeStatusRepository.findByName(status).map(s -> s.getId()).orElse(null)
                : null;

        Specification<Transfer> spec = buildSpec(employeeId, statusId);
        Page<Transfer> page = repository.findAll(spec, pageable);
        long total = repository.count();
        long approved = resolveApprovedCount();

        return PagedResponse.of(page.map(e -> {
            Optional<HRRequest> hrReq = hrRequestRepository.findTopByTransferIdOrderByCreatedAtDesc(e.getId());
            Long reqId = hrReq.map(HRRequest::getId).orElse(null);
            String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
            return toResponse(e, Collections.emptyList(), reqId, statusName);
        }), total, approved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponse getById(Long id) {
        Transfer entity = findOrThrow(id);
        Optional<HRRequest> hrReq = hrRequestRepository.findTopByTransferIdOrderByCreatedAtDesc(id);
        Long requestId = hrReq.map(HRRequest::getId).orElse(null);
        String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
        return toResponse(entity, fetchDocuments(id), requestId, statusName);
    }

    @Override
    @Transactional
    public TransferResponse create(TransferRequest request, List<MultipartFile> files) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + request.getEmployeeId()));

        if (employee.getCostCenter() == null) {
            throw new IllegalStateException("El empleado no tiene centro de costo asignado");
        }
        if (employee.getCostCenter().equals(request.getToCostCenter())) {
            throw new IllegalStateException("El centro de costo destino es igual al actual");
        }

        Transfer entity = Transfer.builder()
                .employeeId(request.getEmployeeId())
                .fromCostCenter(employee.getCostCenter())
                .toCostCenter(request.getToCostCenter())
                .effectiveDate(request.getEffectiveDate())
                .reason(request.getReason())
                .build();

        Transfer saved = repository.save(entity);
        HRRequest hrReq = hrRequestService.createForTransfer(saved.getId(), saved.getEmployeeId(), "CREATE", null);

        List<FileMetadataResponse> documents = uploadFiles(saved.getId(), saved.getEmployeeId(), files);
        String statusName = resolveStatusName(hrReq.getStatusId());
        return toResponse(saved, documents, hrReq.getId(), statusName);
    }

    @Override
    @Transactional
    public TransferResponse update(UpdateTransferRequest request, List<MultipartFile> files) {
        Transfer entity = findOrThrow(request.getId());

        String proposedData;
        try {
            proposedData = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando proposedData", e);
        }

        HRRequest hrReq = hrRequestService.createForTransfer(entity.getId(), entity.getEmployeeId(), "UPDATE", proposedData);

        // Archivos pendientes de aprobación: se suben con TRANSFER_PENDING + hrRequestId
        uploadPendingFiles(hrReq.getId(), entity.getEmployeeId(), files);

        Long requestId = hrRequestRepository.findTopByTransferIdOrderByCreatedAtDesc(entity.getId())
                .map(HRRequest::getId).orElse(hrReq.getId());

        List<FileMetadataResponse> documents = fetchDocuments(entity.getId());
        String statusName = resolveStatusName(hrReq.getStatusId());
        return toResponse(entity, documents, requestId, statusName);
    }

    @Override
    public void deleteDocument(Long transferId, Long fileId, Long userId) {
        if (!repository.existsById(transferId)) {
            throw new ResourceNotFoundException("Traspaso no encontrado: " + transferId);
        }
        storageService.delete(fileId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Empleado,RUT,Centro Origen,Centro Destino,Fecha Efectiva,Motivo,Estado,Fecha Creación\n");

        repository.findAll().forEach(e -> {
            String statusName = hrRequestRepository.findTopByTransferIdOrderByCreatedAtDesc(e.getId())
                    .map(r -> resolveStatusName(r.getStatusId())).orElse("");
            String fromName = resolveProjectName(e.getFromCostCenter());
            String toName   = resolveProjectName(e.getToCostCenter());

            csv.append(e.getId()).append(",")
               .append(escape(fullName(e.getEmployee()))).append(",")
               .append(e.getEmployee() != null ? e.getEmployee().getIdentification() : "").append(",")
               .append(escape(fromName)).append(",")
               .append(escape(toName)).append(",")
               .append(e.getEffectiveDate() != null ? e.getEffectiveDate() : "").append(",")
               .append(escape(e.getReason())).append(",")
               .append(escape(statusName)).append(",")
               .append(formatDate(e.getCreatedAt())).append("\n");
        });

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ─── File helpers ─────────────────────────────────────────────────────────

    private List<FileMetadataResponse> uploadFiles(Long transferId, Long uploadedBy, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return Collections.emptyList();

        List<FileMetadataResponse> existing = fetchDocuments(transferId);
        if (existing.size() + files.size() > MAX_DOCUMENTS) {
            throw new IllegalArgumentException(
                    "El traspaso ya tiene " + existing.size() + " documento(s). " +
                    "Máximo permitido: " + MAX_DOCUMENTS + ".");
        }
        return fileUploadHelper.uploadFiles(files, uploadedBy, ENTITY_TYPE, transferId);
    }

    private void uploadPendingFiles(Long hrRequestId, Long uploadedBy, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            fileUploadHelper.validateFile(file);
            storageService.upload(file, uploadedBy, "TRANSFER_PENDING", hrRequestId, false);
        }
    }

    private List<FileMetadataResponse> fetchDocuments(Long transferId) {
        try {
            List<FileMetadataResponse> response = storageService.listByEntity(ENTITY_TYPE, transferId);
            return response != null ? response : Collections.emptyList();
        } catch (Exception e) {
            log.warn("No se pudieron obtener documentos del traspaso {}: {}", transferId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Transfer findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Traspaso no encontrado con id: " + id));
    }

    private TransferResponse toResponse(Transfer e, List<FileMetadataResponse> documents,
                                         Long requestId, String statusName) {
        Employee emp = e.getEmployee();
        return TransferResponse.builder()
                .id(e.getId())
                .status(statusName)
                .employeeId(e.getEmployeeId())
                .employeeFullName(fullName(emp))
                .employeeIdentification(emp != null ? emp.getIdentification() : null)
                .fromCostCenter(e.getFromCostCenter())
                .fromCostCenterName(resolveProjectName(e.getFromCostCenter()))
                .toCostCenter(e.getToCostCenter())
                .toCostCenterName(resolveProjectName(e.getToCostCenter()))
                .effectiveDate(e.getEffectiveDate())
                .reason(e.getReason())
                .documents(documents)
                .hrRequestId(requestId)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private Specification<Transfer> buildSpec(Long employeeId, Long statusId) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (employeeId != null) predicates.add(cb.equal(root.get("employeeId"), employeeId));
            if (statusId != null) {
                var subquery = query.subquery(Long.class);
                var hrRoot = subquery.from(com.crm.mcsv_rrhh.entity.HRRequest.class);
                subquery.select(hrRoot.get("transferId"))
                        .where(
                            cb.equal(hrRoot.get("statusId"), statusId),
                            cb.isNotNull(hrRoot.get("transferId"))
                        );
                predicates.add(root.get("id").in(subquery));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private String resolveStatusName(Long statusId) {
        if (statusId == null) return null;
        return employeeStatusRepository.findById(statusId).map(s -> s.getName()).orElse(null);
    }

    private long resolveApprovedCount() {
        return employeeStatusRepository.findByName("Aprobado")
                .map(s -> hrRequestRepository.findAll().stream()
                        .filter(r -> r.getTransferId() != null && s.getId().equals(r.getStatusId()))
                        .map(HRRequest::getTransferId)
                        .distinct()
                        .count())
                .orElse(0L);
    }

    private String resolveProjectName(Integer costCenter) {
        if (costCenter == null) return null;
        try {
            ProjectClient.ProjectNameDTO dto = projectClient.getByCostCenter(costCenter);
            return dto != null ? dto.getName() : null;
        } catch (Exception e) {
            log.warn("No se pudo resolver nombre de proyecto para costCenter={}: {}", costCenter, e.getMessage());
            return null;
        }
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
