package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.BulkImportResult;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.SafetyComplianceRequest;
import com.crm.mcsv_rrhh.dto.SafetyComplianceResponse;
import com.crm.mcsv_rrhh.dto.UpdateSafetyComplianceRequest;
import com.crm.mcsv_rrhh.entity.SafetyCompliance;
import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.SafetyComplianceRepository;
import com.crm.mcsv_rrhh.repository.SafetyComplianceSpecification;
import com.crm.mcsv_rrhh.service.SafetyComplianceService;
import com.crm.common.util.CsvUtil;
import com.crm.common.util.DateRangeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SafetyComplianceServiceImpl implements SafetyComplianceService {

    private final SafetyComplianceRepository repository;

    @Override
    public SafetyComplianceResponse create(SafetyComplianceRequest request) {
        if (repository.existsByName(request.getName()))
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + request.getName());

        return toResponse(repository.save(SafetyCompliance.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build()));
    }

    @Override
    public SafetyComplianceResponse update(UpdateSafetyComplianceRequest request) {
        SafetyCompliance entity = findOrThrow(request.getId());

        if (request.getName() != null &&
                !request.getName().equals(entity.getName()) &&
                repository.existsByNameAndIdNot(request.getName(), request.getId()))
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + request.getName());

        if (request.getName() != null)        entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());

        return toResponse(repository.save(entity));
    }

    @Override
    public void updateStatus(Long id, Boolean active) {
        SafetyCompliance entity = findOrThrow(id);
        entity.setActive(active);
        repository.save(entity);
    }

    @Override
    public SafetyComplianceResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public PagedResponse<SafetyComplianceResponse> list(String search, Boolean active,
                                                         LocalDate createdFrom, LocalDate createdTo,
                                                         LocalDate updatedFrom, LocalDate updatedTo,
                                                         Pageable pageable) {
        LocalDateTime cFrom = DateRangeUtil.startOf(createdFrom);
        LocalDateTime cTo   = DateRangeUtil.endOf(createdTo);
        LocalDateTime uFrom = DateRangeUtil.startOf(updatedFrom);
        LocalDateTime uTo   = DateRangeUtil.endOf(updatedTo);

        Specification<SafetyCompliance> spec =
                SafetyComplianceSpecification.withFilters(search, active, cFrom, cTo, uFrom, uTo);

        Page<SafetyCompliance> page = repository.findAll(spec, pageable);
        long totalActive = repository.count(
                SafetyComplianceSpecification.withFilters(null, true, null, null, null, null));

        return PagedResponse.of(page.map(this::toResponse), page.getTotalElements(), totalActive);
    }

    @Override
    public byte[] exportCsv() {
        return CsvUtil.build(
                "ID,Nombre,Descripción,Activo,Fecha Creación,Fecha Actualización",
                repository.findAll(),
                e -> e.getId() + "," +
                        escape(e.getName()) + "," +
                        escape(e.getDescription()) + "," +
                        e.getActive() + "," +
                        formatDate(e.getCreatedAt()) + "," +
                        formatDate(e.getUpdatedAt()));
    }

    @Override
    public BulkImportResult importFromCsv(MultipartFile file) {
        return CsvUtil.importNameDesc(file, (name, description) -> {
            SafetyComplianceRequest request = new SafetyComplianceRequest();
            request.setName(name);
            request.setDescription(description);
            create(request);
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private SafetyCompliance findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de seguridad no encontrada con id: " + id));
    }

    private SafetyComplianceResponse toResponse(SafetyCompliance e) {
        return SafetyComplianceResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .active(e.getActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
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
