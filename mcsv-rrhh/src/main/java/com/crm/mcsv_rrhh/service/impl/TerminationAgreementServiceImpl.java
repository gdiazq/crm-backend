package com.crm.mcsv_rrhh.service.impl;

import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.TerminationAgreementRequest;
import com.crm.mcsv_rrhh.dto.TerminationAgreementResponse;
import com.crm.mcsv_rrhh.dto.UpdateTerminationAgreementRequest;
import com.crm.mcsv_rrhh.entity.*;
import com.crm.mcsv_rrhh.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.*;
import com.crm.mcsv_rrhh.service.TerminationAgreementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TerminationAgreementServiceImpl implements TerminationAgreementService {

    private final TerminationAgreementRepository repository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final LegalTerminationCauseRepository legalTerminationCauseRepository;
    private final QualityOfWorkRepository qualityOfWorkRepository;
    private final SafetyComplianceRepository safetyComplianceRepository;
    private final NoReHiredCauseRepository noReHiredCauseRepository;
    private final TerminationQuizQuestionRepository quizQuestionRepository;

    // ─── List ─────────────────────────────────────────────────────────────────

    @Override
    public PagedResponse<TerminationAgreementResponse> list(String search, String status,
                                                             Long employeeId, Long legalTerminationCauseId,
                                                             Boolean rehireEligible,
                                                             LocalDate endDateFrom, LocalDate endDateTo,
                                                             LocalDate createdFrom, LocalDate createdTo,
                                                             Pageable pageable) {
        LocalDateTime cFrom = createdFrom != null ? createdFrom.atStartOfDay()   : null;
        LocalDateTime cTo   = createdTo   != null ? createdTo.atTime(23, 59, 59) : null;

        Specification<TerminationAgreement> spec = TerminationAgreementSpecification.withFilters(
                search, status, employeeId, legalTerminationCauseId,
                rehireEligible, endDateFrom, endDateTo, cFrom, cTo);

        Page<TerminationAgreement> page = repository.findAll(spec, pageable);

        long total  = repository.count();
        long signed = repository.count(TerminationAgreementSpecification.withFilters(
                null, "FIRMADO", null, null, null, null, null, null, null));

        return PagedResponse.of(page.map(this::toResponse), total, signed);
    }

    // ─── GetById ──────────────────────────────────────────────────────────────

    @Override
    public TerminationAgreementResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TerminationAgreementResponse create(TerminationAgreementRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + request.getEmployeeId()));

        contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + request.getContractId()));

        TerminationAgreement entity = TerminationAgreement.builder()
                .employeeId(request.getEmployeeId())
                .contractId(request.getContractId())
                .endDate(request.getEndDate())
                .legalTerminationCause(resolveOrNull(request.getLegalTerminationCauseId(),
                        legalTerminationCauseRepository, "Causal legal"))
                .qualityOfWork(resolveOrNull(request.getQualityOfWorkId(),
                        qualityOfWorkRepository, "Calidad de trabajo"))
                .safetyCompliance(resolveOrNull(request.getSafetyComplianceId(),
                        safetyComplianceRepository, "Cumplimiento de seguridad"))
                .rehireEligible(request.getRehireEligible() != null ? request.getRehireEligible() : true)
                .noReHiredCause(request.getRehireEligible() != null && !request.getRehireEligible()
                        ? resolveOrNull(request.getNoReHiredCauseId(), noReHiredCauseRepository, "Causa de no recontratación")
                        : null)
                .terminationDocumentUrl(request.getTerminationDocumentUrl())
                .observations(request.getObservations())
                .hrRequestId(request.getHrRequestId())
                .status("BORRADOR")
                .build();

        TerminationAgreement saved = repository.save(entity);

        // TODO (Parte 3): generar TerminationQuiz por cada pregunta activa
        // quizService.generateQuizForAgreement(saved.getId());

        return toResponse(saved);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TerminationAgreementResponse update(UpdateTerminationAgreementRequest request) {
        TerminationAgreement entity = findOrThrow(request.getId());

        if (!"BORRADOR".equals(entity.getStatus()))
            throw new IllegalStateException("Solo se pueden editar finiquitos en estado BORRADOR");

        if (request.getEndDate() != null)               entity.setEndDate(request.getEndDate());
        if (request.getLegalTerminationCauseId() != null)
            entity.setLegalTerminationCause(resolveOrNull(request.getLegalTerminationCauseId(),
                    legalTerminationCauseRepository, "Causal legal"));
        if (request.getQualityOfWorkId() != null)
            entity.setQualityOfWork(resolveOrNull(request.getQualityOfWorkId(),
                    qualityOfWorkRepository, "Calidad de trabajo"));
        if (request.getSafetyComplianceId() != null)
            entity.setSafetyCompliance(resolveOrNull(request.getSafetyComplianceId(),
                    safetyComplianceRepository, "Cumplimiento de seguridad"));
        if (request.getRehireEligible() != null) {
            entity.setRehireEligible(request.getRehireEligible());
            if (!request.getRehireEligible() && request.getNoReHiredCauseId() != null)
                entity.setNoReHiredCause(resolveOrNull(request.getNoReHiredCauseId(),
                        noReHiredCauseRepository, "Causa de no recontratación"));
            else if (request.getRehireEligible())
                entity.setNoReHiredCause(null);
        }
        if (request.getTerminationDocumentUrl() != null) entity.setTerminationDocumentUrl(request.getTerminationDocumentUrl());
        if (request.getObservations() != null)           entity.setObservations(request.getObservations());
        if (request.getHrRequestId() != null)            entity.setHrRequestId(request.getHrRequestId());

        return toResponse(repository.save(entity));
    }

    // ─── Sign ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void sign(Long id) {
        TerminationAgreement entity = findOrThrow(id);

        if (!"BORRADOR".equals(entity.getStatus()))
            throw new IllegalStateException("Solo se pueden firmar finiquitos en estado BORRADOR");

        // TODO (Parte 3): validar que todas las preguntas requeridas tienen respuesta
        // quizService.validateQuizCompleted(id);

        entity.setStatus("FIRMADO");
        repository.save(entity);
    }

    // ─── Cancel ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void cancel(Long id) {
        TerminationAgreement entity = findOrThrow(id);

        if ("CANCELADO".equals(entity.getStatus()))
            throw new IllegalStateException("El finiquito ya está cancelado");

        entity.setStatus("CANCELADO");
        repository.save(entity);
    }

    // ─── Export CSV ───────────────────────────────────────────────────────────

    @Override
    public byte[] exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Empleado,RUT,Contrato ID,Fecha Término,Causal Legal,Calidad Trabajo," +
                   "Cumplimiento Seguridad,Recontratación,Causa No Recontratación," +
                   "URL Documento,Observaciones,Estado,Fecha Creación\n");

        repository.findAll().forEach(e -> csv
                .append(e.getId()).append(",")
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
                .append(e.getStatus()).append(",")
                .append(formatDate(e.getCreatedAt())).append("\n"));

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private TerminationAgreement findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finiquito no encontrado con id: " + id));
    }

    private <T> T resolveOrNull(Long id, org.springframework.data.jpa.repository.JpaRepository<T, Long> repo,
                                  String label) {
        if (id == null) return null;
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " no encontrado con id: " + id));
    }

    private TerminationAgreementResponse toResponse(TerminationAgreement e) {
        Employee emp = e.getEmployee();
        return TerminationAgreementResponse.builder()
                .id(e.getId())
                .status(e.getStatus())
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
                .terminationDocumentUrl(e.getTerminationDocumentUrl())
                .observations(e.getObservations())
                .hrRequestId(e.getHrRequestId())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
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
