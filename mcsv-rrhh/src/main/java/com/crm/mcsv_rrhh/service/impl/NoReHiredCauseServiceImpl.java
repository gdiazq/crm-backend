package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.BulkImportResult;
import com.crm.mcsv_rrhh.dto.NoReHiredCauseRequest;
import com.crm.mcsv_rrhh.dto.NoReHiredCauseResponse;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.UpdateNoReHiredCauseRequest;
import com.crm.mcsv_rrhh.entity.NoReHiredCause;
import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.NoReHiredCauseRepository;
import com.crm.mcsv_rrhh.repository.NoReHiredCauseSpecification;
import com.crm.mcsv_rrhh.service.NoReHiredCauseService;
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
public class NoReHiredCauseServiceImpl implements NoReHiredCauseService {

    private final NoReHiredCauseRepository repository;

    @Override
    public NoReHiredCauseResponse create(NoReHiredCauseRequest request) {
        if (repository.existsByName(request.getName()))
            throw new DuplicateResourceException("Ya existe una causa con el nombre: " + request.getName());

        return toResponse(repository.save(NoReHiredCause.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build()));
    }

    @Override
    public NoReHiredCauseResponse update(UpdateNoReHiredCauseRequest request) {
        NoReHiredCause entity = findOrThrow(request.getId());

        if (request.getName() != null &&
                !request.getName().equals(entity.getName()) &&
                repository.existsByNameAndIdNot(request.getName(), request.getId()))
            throw new DuplicateResourceException("Ya existe una causa con el nombre: " + request.getName());

        if (request.getName() != null)        entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());

        return toResponse(repository.save(entity));
    }

    @Override
    public void updateStatus(Long id, Boolean active) {
        NoReHiredCause entity = findOrThrow(id);
        entity.setActive(active);
        repository.save(entity);
    }

    @Override
    public NoReHiredCauseResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public PagedResponse<NoReHiredCauseResponse> list(String search, Boolean active,
                                                      LocalDate createdFrom, LocalDate createdTo,
                                                      LocalDate updatedFrom, LocalDate updatedTo,
                                                      Pageable pageable) {
        LocalDateTime cFrom = DateRangeUtil.startOf(createdFrom);
        LocalDateTime cTo   = DateRangeUtil.endOf(createdTo);
        LocalDateTime uFrom = DateRangeUtil.startOf(updatedFrom);
        LocalDateTime uTo   = DateRangeUtil.endOf(updatedTo);

        Specification<NoReHiredCause> spec =
                NoReHiredCauseSpecification.withFilters(search, active, cFrom, cTo, uFrom, uTo);

        Page<NoReHiredCause> page = repository.findAll(spec, pageable);
        long totalActive = repository.count(
                NoReHiredCauseSpecification.withFilters(null, true, null, null, null, null));

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
            NoReHiredCauseRequest request = new NoReHiredCauseRequest();
            request.setName(name);
            request.setDescription(description);
            create(request);
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private NoReHiredCause findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Causa de no recontratación no encontrada con id: " + id));
    }

    private NoReHiredCauseResponse toResponse(NoReHiredCause e) {
        return NoReHiredCauseResponse.builder()
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
