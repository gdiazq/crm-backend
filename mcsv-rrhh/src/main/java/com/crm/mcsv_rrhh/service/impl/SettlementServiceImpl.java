package com.crm.mcsv_rrhh.service.impl;

import com.crm.mcsv_rrhh.client.StorageClient;
import com.crm.mcsv_rrhh.dto.FileMetadataResponse;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.SettlementRequest;
import com.crm.mcsv_rrhh.dto.SettlementResponse;
import com.crm.mcsv_rrhh.dto.UpdateSettlementRequest;
import com.crm.mcsv_rrhh.entity.*;
import com.crm.mcsv_rrhh.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.*;
import com.crm.mcsv_rrhh.service.HRRequestService;
import com.crm.mcsv_rrhh.service.SettlementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementServiceImpl implements SettlementService {

    private static final String ENTITY_TYPE = "SETTLEMENT";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png");

    private final SettlementRepository repository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final LegalTerminationCauseRepository legalTerminationCauseRepository;
    private final QualityOfWorkRepository qualityOfWorkRepository;
    private final SafetyComplianceRepository safetyComplianceRepository;
    private final NoReHiredCauseRepository noReHiredCauseRepository;
    private final TerminationQuizQuestionRepository quizQuestionRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final StorageClient storageClient;
    private final HRRequestService hrRequestService;
    private final HRRequestRepository hrRequestRepository;
    private final ObjectMapper objectMapper;

    @Override
    public PagedResponse<SettlementResponse> list(String search, String status,
                                                   Long legalTerminationCauseId,
                                                   Long qualityOfWorkId,
                                                   Long safetyComplianceId,
                                                   Long noReHiredCauseId,
                                                   Boolean rehireEligible,
                                                   LocalDate endDateFrom, LocalDate endDateTo,
                                                   LocalDate createdFrom, LocalDate createdTo,
                                                   Pageable pageable) {
        LocalDateTime cFrom = createdFrom != null ? createdFrom.atStartOfDay()   : null;
        LocalDateTime cTo   = createdTo   != null ? createdTo.atTime(23, 59, 59) : null;

        Long statusId = status != null && !status.isBlank()
                ? employeeStatusRepository.findByName(status).map(s -> s.getId()).orElse(null)
                : null;

        Specification<Settlement> spec = SettlementSpecification.withFilters(
                search, statusId, legalTerminationCauseId, qualityOfWorkId, safetyComplianceId, noReHiredCauseId,
                rehireEligible, endDateFrom, endDateTo, cFrom, cTo);

        Page<Settlement> page = repository.findAll(spec, pageable);
        long total    = repository.count();
        long approved = resolveApprovedCount();

        return PagedResponse.of(page.map(e -> {
            Optional<HRRequest> hrReq = hrRequestRepository.findTopBySettlementIdOrderByCreatedAtDesc(e.getId());
            Long reqId = hrReq.map(HRRequest::getId).orElse(null);
            String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
            return toResponse(e, Collections.emptyList(), reqId, statusName);
        }), total, approved);
    }

    @Override
    public SettlementResponse getById(Long id) {
        Settlement entity = findOrThrow(id);
        Optional<HRRequest> hrReq = hrRequestRepository.findTopBySettlementIdOrderByCreatedAtDesc(id);
        Long requestId = hrReq.map(HRRequest::getId).orElse(null);
        String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
        return toResponse(entity, fetchDocuments(id), requestId, statusName);
    }

    @Override
    @Transactional
    public SettlementResponse create(SettlementRequest request, List<MultipartFile> files) {
        employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + request.getEmployeeId()));

        List<Contract> contracts = contractRepository.findByEmployeeId(request.getEmployeeId());
        if (contracts.isEmpty())
            throw new ResourceNotFoundException("El empleado no tiene contrato: " + request.getEmployeeId());
        Long contractId = contracts.get(0).getId();

        Settlement entity = Settlement.builder()
                .employeeId(request.getEmployeeId())
                .contractId(contractId)
                .endDate(request.getEndDate())
                .legalTerminationCause(resolveOrNull(request.getLegalTerminationCauseId(), legalTerminationCauseRepository, "Causal legal"))
                .qualityOfWork(resolveOrNull(request.getQualityOfWorkId(), qualityOfWorkRepository, "Calidad de trabajo"))
                .safetyCompliance(resolveOrNull(request.getSafetyComplianceId(), safetyComplianceRepository, "Cumplimiento de seguridad"))
                .rehireEligible(request.getRehireEligible() != null ? request.getRehireEligible() : true)
                .noReHiredCause(request.getRehireEligible() != null && !request.getRehireEligible()
                        ? resolveOrNull(request.getNoReHiredCauseId(), noReHiredCauseRepository, "Causa de no recontratación")
                        : null)
                .observations(request.getObservations())
                .build();

        Settlement saved = repository.save(entity);
        HRRequest hrReq = hrRequestService.createForSettlement(saved.getId(), saved.getEmployeeId(), "CREATE", null);

        // TODO (Parte 3): generar SettlementQuiz por cada pregunta activa
        // quizService.generateQuizForSettlement(saved.getId());

        List<FileMetadataResponse> documents = uploadFiles(saved.getId(), saved.getEmployeeId(), files);
        String statusName = resolveStatusName(hrReq.getStatusId());
        return toResponse(saved, documents, hrReq.getId(), statusName);
    }

    @Override
    @Transactional
    public SettlementResponse update(UpdateSettlementRequest request, List<MultipartFile> files) {
        Settlement entity = findOrThrow(request.getId());

        String proposedData;
        try {
            proposedData = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando proposedData", e);
        }

        HRRequest hrReq = hrRequestService.createForSettlement(entity.getId(), entity.getEmployeeId(), "UPDATE", proposedData);
        repository.save(entity);

        uploadFiles(entity.getId(), entity.getEmployeeId(), files);
        List<FileMetadataResponse> documents = fetchDocuments(entity.getId());
        String statusName = resolveStatusName(hrReq.getStatusId());
        return toResponse(entity, documents, hrReq.getId(), statusName);
    }

    @Override
    public byte[] exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Empleado,RUT,Contrato ID,Fecha Término,Causal Legal,Calidad Trabajo," +
                   "Cumplimiento Seguridad,Recontratación,Causa No Recontratación," +
                   "URL Documento,Observaciones,Estado,Fecha Creación\n");

        repository.findAll().forEach(e -> {
            String statusName = hrRequestRepository.findTopBySettlementIdOrderByCreatedAtDesc(e.getId())
                    .map(r -> resolveStatusName(r.getStatusId())).orElse("");
            csv.append(e.getId()).append(",")
               .append(escape(fullName(e.getEmployee()))).append(",")
               .append(e.getEmployee() != null ? e.getEmployee().getIdentification() : "").append(",")
               .append(e.getContractId()).append(",")
               .append(e.getEndDate() != null ? e.getEndDate() : "").append(",")
               .append(e.getLegalTerminationCause() != null ? escape(e.getLegalTerminationCause().getName()) : "").append(",")
               .append(e.getQualityOfWork() != null ? escape(e.getQualityOfWork().getName()) : "").append(",")
               .append(e.getSafetyCompliance() != null ? escape(e.getSafetyCompliance().getName()) : "").append(",")
               .append(e.getRehireEligible()).append(",")
               .append(e.getNoReHiredCause() != null ? escape(e.getNoReHiredCause().getName()) : "").append(",")
               .append(escape(e.getTerminationDocumentUrl())).append(",")
               .append(escape(e.getObservations())).append(",")
               .append(escape(statusName)).append(",")
               .append(formatDate(e.getCreatedAt())).append("\n");
        });

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Settlement findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finiquito no encontrado con id: " + id));
    }

    private <T> T resolveOrNull(Long id, org.springframework.data.jpa.repository.JpaRepository<T, Long> repo, String label) {
        if (id == null) return null;
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " no encontrado con id: " + id));
    }

    private String resolveStatusName(Long statusId) {
        if (statusId == null) return null;
        return employeeStatusRepository.findById(statusId).map(s -> s.getName()).orElse(null);
    }

    private long resolveApprovedCount() {
        return employeeStatusRepository.findByName("Aprobado")
                .map(s -> hrRequestRepository.countSettlementsWithLatestStatusId(s.getId()))
                .orElse(0L);
    }

    private SettlementResponse toResponse(Settlement e, List<FileMetadataResponse> documents,
                                           Long requestId, String statusName) {
        Employee emp = e.getEmployee();
        return SettlementResponse.builder()
                .id(e.getId())
                .status(statusName)
                .employeeId(e.getEmployeeId())
                .employeeFullName(fullName(emp))
                .employeeIdentification(emp != null ? emp.getIdentification() : null)
                .contractId(e.getContractId())
                .endDate(e.getEndDate())
                .legalTerminationCauseId(e.getLegalTerminationCause() != null ? e.getLegalTerminationCause().getId() : null)
                .legalTerminationCauseName(e.getLegalTerminationCause() != null ? e.getLegalTerminationCause().getName() : null)
                .qualityOfWorkId(e.getQualityOfWork() != null ? e.getQualityOfWork().getId() : null)
                .qualityOfWorkName(e.getQualityOfWork() != null ? e.getQualityOfWork().getName() : null)
                .safetyComplianceId(e.getSafetyCompliance() != null ? e.getSafetyCompliance().getId() : null)
                .safetyComplianceName(e.getSafetyCompliance() != null ? e.getSafetyCompliance().getName() : null)
                .rehireEligible(e.getRehireEligible())
                .noReHiredCauseId(e.getNoReHiredCause() != null ? e.getNoReHiredCause().getId() : null)
                .noReHiredCauseName(e.getNoReHiredCause() != null ? e.getNoReHiredCause().getName() : null)
                .documents(documents)
                .observations(e.getObservations())
                .hrRequestId(requestId)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private List<FileMetadataResponse> uploadFiles(Long settlementId, Long uploadedBy, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return Collections.emptyList();
        List<FileMetadataResponse> uploaded = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE)
                throw new IllegalArgumentException(
                        "El archivo '" + file.getOriginalFilename() + "' supera el límite de 10MB.");
            if (!ALLOWED_TYPES.contains(file.getContentType()))
                throw new IllegalArgumentException(
                        "El archivo '" + file.getOriginalFilename() + "' no es un formato permitido. Use PDF, JPG o PNG.");
            ResponseEntity<FileMetadataResponse> response =
                    storageClient.upload(file, uploadedBy, ENTITY_TYPE, settlementId, false);
            if (response.getBody() != null) uploaded.add(response.getBody());
        }
        return uploaded;
    }

    private List<FileMetadataResponse> fetchDocuments(Long settlementId) {
        try {
            ResponseEntity<List<FileMetadataResponse>> response =
                    storageClient.listByEntity(ENTITY_TYPE, settlementId);
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("No se pudieron obtener documentos del finiquito {}: {}", settlementId, e.getMessage());
            return Collections.emptyList();
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
