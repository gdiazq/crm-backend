package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.dto.AttendanceStatusRequest;
import com.crm.mcsv_rrhh.dto.AttendanceStatusResponse;
import com.crm.mcsv_rrhh.dto.UpdateAttendanceStatusRequest;
import com.crm.mcsv_rrhh.entity.AttendanceStatus;
import com.crm.mcsv_rrhh.repository.AttendanceStatusRepository;
import com.crm.mcsv_rrhh.repository.AttendanceStatusSpecification;
import com.crm.mcsv_rrhh.service.AttendanceStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceStatusServiceImpl implements AttendanceStatusService {

    private final AttendanceStatusRepository repository;

    @Override
    public AttendanceStatusResponse create(AttendanceStatusRequest request) {
        validateUnique(request.getName(), request.getCode(), null);
        AttendanceStatus entity = AttendanceStatus.builder()
                .name(request.getName())
                .code(normalizeCode(request.getCode()))
                .description(request.getDescription())
                .active(true)
                .build();
        return toResponse(repository.save(entity));
    }

    @Override
    public AttendanceStatusResponse update(UpdateAttendanceStatusRequest request) {
        AttendanceStatus entity = findOrThrow(request.getId());
        String nextName = request.getName() != null ? request.getName() : entity.getName();
        String nextCode = request.getCode() != null ? normalizeCode(request.getCode()) : entity.getCode();
        validateUnique(nextName, nextCode, entity.getId());

        if (request.getName() != null) entity.setName(request.getName());
        if (request.getCode() != null) entity.setCode(nextCode);
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        return toResponse(repository.save(entity));
    }

    @Override
    public void updateStatus(Long id, Boolean active) {
        AttendanceStatus entity = findOrThrow(id);
        entity.setActive(active);
        repository.save(entity);
    }

    @Override
    public AttendanceStatusResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public PagedResponse<AttendanceStatusResponse> list(String search, Boolean active,
                                                         LocalDate createdFrom, LocalDate createdTo,
                                                         LocalDate updatedFrom, LocalDate updatedTo,
                                                         Pageable pageable) {
        Specification<AttendanceStatus> spec = AttendanceStatusSpecification.withFilters(
                search, active, createdFrom, createdTo, updatedFrom, updatedTo);
        Page<AttendanceStatus> page = repository.findAll(spec, pageable);
        return PagedResponse.of(page.map(this::toResponse), page.getTotalElements(), repository.countByActiveTrue());
    }

    @Override
    public List<AttendanceStatusResponse> selectActive() {
        return repository.findAll().stream()
                .filter(status -> Boolean.TRUE.equals(status.getActive()))
                .map(this::toResponse)
                .toList();
    }

    private AttendanceStatus findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de asistencia no encontrado: " + id));
    }

    private void validateUnique(String name, String code, Long id) {
        if (id == null) {
            if (repository.existsByName(name)) throw new DuplicateResourceException("Ya existe un estado con el nombre: " + name);
            if (repository.existsByCode(normalizeCode(code))) throw new DuplicateResourceException("Ya existe un estado con el código: " + code);
            return;
        }
        if (repository.existsByNameAndIdNot(name, id)) throw new DuplicateResourceException("Ya existe un estado con el nombre: " + name);
        if (repository.existsByCodeAndIdNot(normalizeCode(code), id)) throw new DuplicateResourceException("Ya existe un estado con el código: " + code);
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private AttendanceStatusResponse toResponse(AttendanceStatus status) {
        return AttendanceStatusResponse.builder()
                .id(status.getId())
                .name(status.getName())
                .code(status.getCode())
                .description(status.getDescription())
                .active(status.getActive())
                .createdAt(status.getCreatedAt())
                .updatedAt(status.getUpdatedAt())
                .build();
    }
}
