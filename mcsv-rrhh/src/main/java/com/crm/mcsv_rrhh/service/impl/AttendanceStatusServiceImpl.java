package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.dto.AttendanceStatusResponse;
import com.crm.mcsv_rrhh.entity.AttendanceStatus;
import com.crm.mcsv_rrhh.repository.AttendanceStatusRepository;
import com.crm.mcsv_rrhh.service.AttendanceStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceStatusServiceImpl implements AttendanceStatusService {

    private final AttendanceStatusRepository repository;

    @Override
    public void updateStatus(Long id, Boolean active) {
        AttendanceStatus entity = findOrThrow(id);
        entity.setActive(active);
        repository.save(entity);
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
