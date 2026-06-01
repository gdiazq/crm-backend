package com.crm.mcsv_rrhh.service.attendance.impl;

import com.crm.mcsv_rrhh.dto.attendance.AttendanceStatusResponse;
import com.crm.mcsv_rrhh.entity.attendance.AttendanceStatus;
import com.crm.mcsv_rrhh.repository.attendance.AttendanceStatusRepository;
import com.crm.mcsv_rrhh.service.attendance.AttendanceStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceStatusServiceImpl implements AttendanceStatusService {

    private final AttendanceStatusRepository repository;

    @Override
    public List<AttendanceStatusResponse> selectActive() {
        return repository.findAll().stream()
                .filter(status -> Boolean.TRUE.equals(status.getActive()))
                .map(this::toResponse)
                .toList();
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
