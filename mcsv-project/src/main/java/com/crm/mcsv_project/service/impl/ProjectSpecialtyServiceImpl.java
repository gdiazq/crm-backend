package com.crm.mcsv_project.service.impl;

import com.crm.mcsv_project.dto.BulkImportResult;
import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectSpecialtyRequest;
import com.crm.mcsv_project.dto.ProjectSpecialtyResponse;
import com.crm.mcsv_project.dto.UpdateProjectSpecialtyRequest;
import com.crm.mcsv_project.entity.ProjectSpecialty;
import com.crm.mcsv_project.exception.DuplicateResourceException;
import com.crm.mcsv_project.exception.ResourceNotFoundException;
import com.crm.mcsv_project.repository.ProjectSpecialtyRepository;
import com.crm.mcsv_project.service.ProjectSpecialtyService;
import com.crm.mcsv_project.util.CsvUtil;
import com.crm.mcsv_project.util.DateRangeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crm.mcsv_project.repository.ProjectSpecialtySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectSpecialtyServiceImpl implements ProjectSpecialtyService {

    private final ProjectSpecialtyRepository repository;

    @Override
    public ProjectSpecialtyResponse create(ProjectSpecialtyRequest request) {
        if (repository.existsByName(request.getName()))
            throw new DuplicateResourceException("Ya existe una especialidad con el nombre: " + request.getName());

        ProjectSpecialty entity = ProjectSpecialty.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();

        return toResponse(repository.save(entity));
    }

    @Override
    public ProjectSpecialtyResponse update(UpdateProjectSpecialtyRequest request) {
        ProjectSpecialty entity = repository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada: " + request.getId()));

        if (request.getName() != null) entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());

        return toResponse(repository.save(entity));
    }

    @Override
    public void updateStatus(Long id, Boolean active) {
        ProjectSpecialty entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada: " + id));
        entity.setActive(active);
        repository.save(entity);
    }

    @Override
    public ProjectSpecialtyResponse getById(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada: " + id)));
    }

    @Override
    public PagedResponse<ProjectSpecialtyResponse> list(String search, Boolean active, LocalDate createdFrom, LocalDate createdTo, LocalDate updatedFrom, LocalDate updatedTo, Pageable pageable) {
        Specification<ProjectSpecialty> spec = ProjectSpecialtySpecification.withFilters(
                search, active,
                DateRangeUtil.startOf(createdFrom), DateRangeUtil.endOf(createdTo),
                DateRangeUtil.startOf(updatedFrom), DateRangeUtil.endOf(updatedTo));
        Page<ProjectSpecialty> page = repository.findAll(spec, pageable);
        long totalActive = repository.count(ProjectSpecialtySpecification.withFilters(null, true, null, null, null, null));
        return PagedResponse.of(page.map(this::toResponse), page.getTotalElements(), totalActive);
    }

    @Override
    public byte[] exportCsv() {
        return CsvUtil.build(
                "ID,Nombre,Descripción,Activo,Fecha Creación,Fecha Actualización",
                repository.findAll(),
                e -> e.getId() + "," + escape(e.getName()) + "," + escape(e.getDescription()) + ","
                        + e.getActive() + "," + formatDate(e.getCreatedAt()) + "," + formatDate(e.getUpdatedAt()));
    }

    @Override
    public BulkImportResult importFromCsv(MultipartFile file) {
        return CsvUtil.importNameDesc(file, (name, description) -> {
            ProjectSpecialtyRequest request = new ProjectSpecialtyRequest();
            request.setName(name);
            request.setDescription(description);
            create(request);
        });
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }

    private String formatDate(java.time.LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private ProjectSpecialtyResponse toResponse(ProjectSpecialty e) {
        return ProjectSpecialtyResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .active(e.getActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
