package com.crm.mcsv_project.service.impl;

import com.crm.mcsv_project.dto.BulkImportResult;
import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectTypeRequest;
import com.crm.mcsv_project.dto.ProjectTypeResponse;
import com.crm.mcsv_project.dto.UpdateProjectTypeRequest;
import com.crm.mcsv_project.entity.ProjectType;
import com.crm.mcsv_project.exception.DuplicateResourceException;
import com.crm.mcsv_project.exception.ResourceNotFoundException;
import com.crm.mcsv_project.repository.ProjectTypeRepository;
import com.crm.mcsv_project.service.ProjectTypeService;
import com.crm.mcsv_project.util.CsvUtil;
import com.crm.mcsv_project.util.DateRangeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crm.mcsv_project.repository.ProjectTypeSpecification;
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
public class ProjectTypeServiceImpl implements ProjectTypeService {

    private final ProjectTypeRepository repository;

    @Override
    public ProjectTypeResponse create(ProjectTypeRequest request) {
        if (repository.existsByName(request.getName()))
            throw new DuplicateResourceException("Ya existe un tipo de proyecto con el nombre: " + request.getName());

        ProjectType entity = ProjectType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();

        return toResponse(repository.save(entity));
    }

    @Override
    public ProjectTypeResponse update(UpdateProjectTypeRequest request) {
        ProjectType entity = repository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de proyecto no encontrado: " + request.getId()));

        if (request.getName() != null) entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());

        return toResponse(repository.save(entity));
    }

    @Override
    public void updateStatus(Long id, Boolean active) {
        ProjectType entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de proyecto no encontrado: " + id));
        entity.setActive(active);
        repository.save(entity);
    }

    @Override
    public ProjectTypeResponse getById(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de proyecto no encontrado: " + id)));
    }

    @Override
    public PagedResponse<ProjectTypeResponse> list(String search, Boolean active, LocalDate createdFrom, LocalDate createdTo, LocalDate updatedFrom, LocalDate updatedTo, Pageable pageable) {
        Specification<ProjectType> spec = ProjectTypeSpecification.withFilters(
                search, active,
                DateRangeUtil.startOf(createdFrom), DateRangeUtil.endOf(createdTo),
                DateRangeUtil.startOf(updatedFrom), DateRangeUtil.endOf(updatedTo));
        Page<ProjectType> page = repository.findAll(spec, pageable);
        long totalActive = repository.count(ProjectTypeSpecification.withFilters(null, true, null, null, null, null));
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
            ProjectTypeRequest request = new ProjectTypeRequest();
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

    private ProjectTypeResponse toResponse(ProjectType e) {
        return ProjectTypeResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .active(e.getActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
