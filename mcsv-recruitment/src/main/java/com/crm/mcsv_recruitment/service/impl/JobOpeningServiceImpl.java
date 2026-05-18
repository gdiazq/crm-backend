package com.crm.mcsv_recruitment.service.impl;

import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_recruitment.client.ProjectClient;
import com.crm.mcsv_recruitment.client.RrhhClient;
import com.crm.mcsv_recruitment.dto.CatalogItem;
import com.crm.mcsv_recruitment.dto.JobOpeningRequest;
import com.crm.mcsv_recruitment.dto.JobOpeningResponse;
import com.crm.mcsv_recruitment.dto.UpdateJobOpeningRequest;
import com.crm.mcsv_recruitment.entity.JobOpening;
import com.crm.mcsv_recruitment.entity.JobOpeningStatus;
import com.crm.mcsv_recruitment.enums.JobOpeningStatusName;
import com.crm.mcsv_recruitment.repository.JobOpeningRepository;
import com.crm.mcsv_recruitment.repository.JobOpeningSpecification;
import com.crm.mcsv_recruitment.repository.JobOpeningStatusRepository;
import com.crm.mcsv_recruitment.service.JobOpeningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobOpeningServiceImpl implements JobOpeningService {

    private final JobOpeningRepository jobOpeningRepository;
    private final JobOpeningStatusRepository jobOpeningStatusRepository;
    private final ProjectClient projectClient;
    private final RrhhClient rrhhClient;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<JobOpeningResponse> list(String search,
                                                  Long statusId,
                                                  Integer costCenter,
                                                  Long supervisorUserId,
                                                  LocalDate closeDateFrom,
                                                  LocalDate closeDateTo,
                                                  LocalDateTime createdFrom,
                                                  LocalDateTime createdTo,
                                                  Pageable pageable) {
        Page<JobOpening> page = jobOpeningRepository.findAll(
                JobOpeningSpecification.withFilters(search, statusId, costCenter, supervisorUserId,
                        closeDateFrom, closeDateTo, createdFrom, createdTo),
                pageable);

        long total = page.getTotalElements();
        long active = resolveCountByStatusName(JobOpeningStatusName.OPEN);
        long pending = resolveCountByStatusName(JobOpeningStatusName.DRAFT);

        return PagedResponse.of(page.map(this::toResponse), total, active, pending);
    }

    @Override
    @Transactional(readOnly = true)
    public JobOpeningResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public JobOpeningResponse create(JobOpeningRequest request, Long currentUserId) {
        JobOpeningStatus draft = jobOpeningStatusRepository
                .findByName(JobOpeningStatusName.DRAFT.getDisplayName())
                .orElseThrow(() -> new IllegalStateException(
                        "Catálogo job_opening_statuses sin estado DRAFT (revisar seed)"));

        validateJobTitle(request.getJobTitleId());
        validateCostCenter(request.getCostCenter());

        JobOpening entity = JobOpening.builder()
                .title(request.getTitle())
                .jobTitleId(request.getJobTitleId())
                .costCenter(request.getCostCenter())
                .headcount(request.getHeadcount())
                .supervisorUserId(request.getSupervisorUserId())
                .referenceSalary(request.getReferenceSalary())
                .closeDate(request.getCloseDate())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .statusId(draft.getId())
                .createdBy(currentUserId)
                .build();

        JobOpening saved = jobOpeningRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public JobOpeningResponse update(UpdateJobOpeningRequest request, Long currentUserId) {
        JobOpening entity = findOrThrow(request.getId());

        if (request.getJobTitleId() != null) {
            validateJobTitle(request.getJobTitleId());
            entity.setJobTitleId(request.getJobTitleId());
        }
        if (request.getCostCenter() != null) {
            validateCostCenter(request.getCostCenter());
            entity.setCostCenter(request.getCostCenter());
        }
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getHeadcount() != null) entity.setHeadcount(request.getHeadcount());
        if (request.getSupervisorUserId() != null) entity.setSupervisorUserId(request.getSupervisorUserId());
        if (request.getReferenceSalary() != null) entity.setReferenceSalary(request.getReferenceSalary());
        if (request.getCloseDate() != null) entity.setCloseDate(request.getCloseDate());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getRequirements() != null) entity.setRequirements(request.getRequirements());

        JobOpening saved = jobOpeningRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        JobOpening entity = findOrThrow(id);

        Long draftStatusId = jobOpeningStatusRepository
                .findByName(JobOpeningStatusName.DRAFT.getDisplayName())
                .map(JobOpeningStatus::getId)
                .orElseThrow(() -> new IllegalStateException(
                        "Catálogo job_opening_statuses sin estado DRAFT (revisar seed)"));

        if (!draftStatusId.equals(entity.getStatusId())) {
            throw new IllegalStateException("Solo se pueden eliminar vacantes en estado DRAFT");
        }

        jobOpeningRepository.delete(entity);
    }

    private JobOpening findOrThrow(Long id) {
        return jobOpeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacante no encontrada: " + id));
    }

    private void validateJobTitle(Long jobTitleId) {
        CatalogItem jobTitle = rrhhClient.getJobTitleById(jobTitleId);
        if (jobTitle == null || jobTitle.id() == null) {
            throw new IllegalArgumentException("Cargo no encontrado: " + jobTitleId);
        }
    }

    private void validateCostCenter(Integer costCenter) {
        ProjectClient.ProjectNameDTO project = projectClient.getByCostCenter(costCenter);
        if (project == null || project.getId() == null) {
            throw new IllegalArgumentException("Centro de costo inválido: " + costCenter);
        }
    }

    private long resolveCountByStatusName(JobOpeningStatusName name) {
        return jobOpeningStatusRepository.findByName(name.getDisplayName())
                .map(s -> jobOpeningRepository.countByStatusId(s.getId()))
                .orElse(0L);
    }

    private JobOpeningResponse toResponse(JobOpening e) {
        String jobTitleName = null;
        try {
            CatalogItem item = rrhhClient.getJobTitleById(e.getJobTitleId());
            if (item != null) jobTitleName = item.name();
        } catch (Exception ex) {
            log.warn("No se pudo resolver jobTitleId {}: {}", e.getJobTitleId(), ex.getMessage());
        }

        String projectName = null;
        try {
            ProjectClient.ProjectNameDTO project = projectClient.getByCostCenter(e.getCostCenter());
            if (project != null) projectName = project.getName();
        } catch (Exception ex) {
            log.warn("No se pudo resolver costCenter {}: {}", e.getCostCenter(), ex.getMessage());
        }

        String statusName = e.getStatus() != null ? e.getStatus().getName() : null;

        return JobOpeningResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .jobTitleId(e.getJobTitleId())
                .jobTitleName(jobTitleName)
                .costCenter(e.getCostCenter())
                .projectName(projectName)
                .headcount(e.getHeadcount())
                .supervisorUserId(e.getSupervisorUserId())
                .supervisorName(null)
                .referenceSalary(e.getReferenceSalary())
                .closeDate(e.getCloseDate())
                .description(e.getDescription())
                .requirements(e.getRequirements())
                .statusId(e.getStatusId())
                .statusName(statusName)
                .createdBy(e.getCreatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
