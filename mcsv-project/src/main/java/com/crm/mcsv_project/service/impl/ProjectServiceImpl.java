package com.crm.mcsv_project.service.impl;

import com.crm.mcsv_project.client.PersonSelectItem;
import com.crm.mcsv_project.client.RrhhClient;
import com.crm.mcsv_project.client.UserClient;
import com.crm.mcsv_project.dto.BulkImportResult;
import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectRequest;
import com.crm.mcsv_project.dto.ProjectResponse;
import com.crm.mcsv_project.dto.UpdateProjectRequest;
import com.crm.mcsv_project.entity.Project;
import com.crm.mcsv_project.entity.ProjectSpecialty;
import com.crm.mcsv_project.entity.ProjectStatus;
import com.crm.mcsv_project.entity.ProjectType;
import com.crm.mcsv_project.exception.DuplicateResourceException;
import com.crm.mcsv_project.exception.ResourceNotFoundException;
import com.crm.mcsv_project.repository.ProjectRepository;
import com.crm.mcsv_project.repository.ProjectSpecification;
import com.crm.mcsv_project.repository.ProjectSpecialtyRepository;
import com.crm.mcsv_project.repository.ProjectStatusRepository;
import com.crm.mcsv_project.repository.ProjectTypeRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements com.crm.mcsv_project.service.ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final ProjectStatusRepository projectStatusRepository;
    private final ProjectSpecialtyRepository projectSpecialtyRepository;
    private final UserClient userClient;
    private final RrhhClient rrhhClient;

    // ─── List ─────────────────────────────────────────────────────────────────

    @Override
    public PagedResponse<ProjectResponse> list(String search, Boolean active,
                                                Long typeId, Long statusId, Long specialtyId,
                                                LocalDate createdFrom, LocalDate createdTo,
                                                LocalDate updatedFrom, LocalDate updatedTo,
                                                Pageable pageable) {
        LocalDateTime cFrom = createdFrom  != null ? createdFrom.atStartOfDay()      : null;
        LocalDateTime cTo   = createdTo    != null ? createdTo.atTime(23, 59, 59)    : null;
        LocalDateTime uFrom = updatedFrom  != null ? updatedFrom.atStartOfDay()      : null;
        LocalDateTime uTo   = updatedTo    != null ? updatedTo.atTime(23, 59, 59)    : null;

        Specification<Project> spec = ProjectSpecification.withFilters(
                search, active, typeId, statusId, specialtyId, cFrom, cTo, uFrom, uTo);

        Page<Project> page = projectRepository.findAll(spec, pageable);
        long totalActive = projectRepository.count(
                ProjectSpecification.withFilters(null, true, null, null, null, null, null, null, null));

        Map<Long, String> visitorMap      = fetchVisitorMap();
        Map<Long, String> supervisorMap   = fetchSupervisorMap();
        Map<Long, String> companyRepMap   = fetchCompanyRepMap();

        return PagedResponse.of(page.map(p -> toResponse(p, visitorMap, supervisorMap, companyRepMap)),
                page.getTotalElements(), totalActive);
    }

    // ─── GetById ──────────────────────────────────────────────────────────────

    @Override
    public ProjectResponse getById(Long id) {
        Project project = findOrThrow(id);
        return toResponse(project, fetchVisitorMap(), fetchSupervisorMap(), fetchCompanyRepMap());
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    public ProjectResponse create(ProjectRequest request) {
        if (projectRepository.existsByCostCenter(request.getCostCenter()))
            throw new DuplicateResourceException("Ya existe un proyecto con el centro de costo: " + request.getCostCenter());

        Project project = Project.builder()
                .costCenter(request.getCostCenter())
                .name(request.getName())
                .address(request.getAddress())
                .description(request.getDescription())
                .type(resolveType(request.getTypeId()))
                .status(resolveStatus(request.getStatusId()))
                .specialty(resolveSpecialty(request.getSpecialtyId()))
                .visitorId(request.getVisitorId())
                .supervisorId(request.getSupervisorId())
                .companyRepresentativeIds(request.getCompanyRepresentativeIds() != null
                        ? request.getCompanyRepresentativeIds()
                        : new ArrayList<>())
                .startDate(request.getStartDate())
                .realStartDate(request.getRealStartDate())
                .endDate(request.getEndDate())
                .realEndDate(request.getRealEndDate())
                .active(true)
                .build();

        return toResponse(projectRepository.save(project),
                fetchVisitorMap(), fetchSupervisorMap(), fetchCompanyRepMap());
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    public ProjectResponse update(UpdateProjectRequest request) {
        Project project = findOrThrow(request.getId());

        if (request.getCostCenter() != null &&
                !request.getCostCenter().equals(project.getCostCenter()) &&
                projectRepository.existsByCostCenterAndIdNot(request.getCostCenter(), request.getId()))
            throw new DuplicateResourceException("Ya existe un proyecto con el centro de costo: " + request.getCostCenter());

        if (request.getCostCenter() != null)   project.setCostCenter(request.getCostCenter());
        if (request.getName() != null)         project.setName(request.getName());
        if (request.getAddress() != null)      project.setAddress(request.getAddress());
        if (request.getDescription() != null)  project.setDescription(request.getDescription());
        if (request.getTypeId() != null)       project.setType(resolveType(request.getTypeId()));
        if (request.getStatusId() != null)     project.setStatus(resolveStatus(request.getStatusId()));
        if (request.getSpecialtyId() != null)  project.setSpecialty(resolveSpecialty(request.getSpecialtyId()));
        if (request.getVisitorId() != null)    project.setVisitorId(request.getVisitorId());
        if (request.getSupervisorId() != null) project.setSupervisorId(request.getSupervisorId());
        if (request.getCompanyRepresentativeIds() != null)
            project.setCompanyRepresentativeIds(request.getCompanyRepresentativeIds());
        if (request.getStartDate() != null)     project.setStartDate(request.getStartDate());
        if (request.getRealStartDate() != null) project.setRealStartDate(request.getRealStartDate());
        if (request.getEndDate() != null)       project.setEndDate(request.getEndDate());
        if (request.getRealEndDate() != null)   project.setRealEndDate(request.getRealEndDate());

        return toResponse(projectRepository.save(project),
                fetchVisitorMap(), fetchSupervisorMap(), fetchCompanyRepMap());
    }

    @Override
    public void updateStatus(Long id, Boolean active) {
        Project project = findOrThrow(id);
        project.setActive(active);
        projectRepository.save(project);
    }

    // ─── Export/Import ────────────────────────────────────────────────────────

    @Override
    public byte[] exportCsv() {
        throw new UnsupportedOperationException("Implemented in Fase 7");
    }

    @Override
    public BulkImportResult importFromCsv(MultipartFile file) {
        throw new UnsupportedOperationException("Implemented in Fase 7");
    }

    // ─── Feign helpers ────────────────────────────────────────────────────────

    private Map<Long, String> fetchVisitorMap() {
        try {
            return userClient.getVisitors().stream()
                    .collect(Collectors.toMap(PersonSelectItem::id, PersonSelectItem::name));
        } catch (Exception e) {
            log.warn("No se pudieron obtener visitadores: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, String> fetchSupervisorMap() {
        try {
            return rrhhClient.getSupervisors().stream()
                    .collect(Collectors.toMap(PersonSelectItem::id, PersonSelectItem::name));
        } catch (Exception e) {
            log.warn("No se pudieron obtener supervisores: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, String> fetchCompanyRepMap() {
        try {
            return rrhhClient.getCompanyRepresentatives().stream()
                    .collect(Collectors.toMap(PersonSelectItem::id, PersonSelectItem::name));
        } catch (Exception e) {
            log.warn("No se pudieron obtener representantes de empresa: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private ProjectResponse toResponse(Project p,
                                        Map<Long, String> visitorMap,
                                        Map<Long, String> supervisorMap,
                                        Map<Long, String> companyRepMap) {
        return ProjectResponse.builder()
                .id(p.getId())
                .costCenter(p.getCostCenter())
                .name(p.getName())
                .address(p.getAddress())
                .description(p.getDescription())
                .typeId(p.getType() != null ? p.getType().getId() : null)
                .statusId(p.getStatus() != null ? p.getStatus().getId() : null)
                .specialtyId(p.getSpecialty() != null ? p.getSpecialty().getId() : null)
                .visitorId(p.getVisitorId())
                .visitorName(p.getVisitorId() != null ? visitorMap.getOrDefault(p.getVisitorId(), null) : null)
                .supervisorId(p.getSupervisorId())
                .supervisorName(p.getSupervisorId() != null ? supervisorMap.getOrDefault(p.getSupervisorId(), null) : null)
                .companyRepresentativeIds(p.getCompanyRepresentativeIds())
                .startDate(p.getStartDate())
                .realStartDate(p.getRealStartDate())
                .endDate(p.getEndDate())
                .realEndDate(p.getRealEndDate())
                .active(p.getActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Project findOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + id));
    }

    private ProjectType resolveType(Long typeId) {
        if (typeId == null) return null;
        return projectTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de proyecto no encontrado: " + typeId));
    }

    private ProjectStatus resolveStatus(Long statusId) {
        if (statusId == null) return null;
        return projectStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de proyecto no encontrado: " + statusId));
    }

    private ProjectSpecialty resolveSpecialty(Long specialtyId) {
        if (specialtyId == null) return null;
        return projectSpecialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad de proyecto no encontrada: " + specialtyId));
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
