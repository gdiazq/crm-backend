package com.crm.mcsv_rrhh.service.employee.impl;

import com.crm.mcsv_rrhh.dto.employee.EmployeeStatusResponse;
import com.crm.mcsv_rrhh.entity.employee.EmployeeStatus;
import com.crm.mcsv_rrhh.enums.hrrequest.RequestStatus;
import com.crm.mcsv_rrhh.repository.employee.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.service.employee.EmployeeStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeStatusServiceImpl implements EmployeeStatusService {

    private static final Set<String> APPROVAL_STATUSES = RequestStatus.displayNamesOf(
            RequestStatus.PENDING_REVIEW,
            RequestStatus.PENDING_APPROVAL,
            RequestStatus.APPROVED,
            RequestStatus.REJECTED
    );

    private final EmployeeStatusRepository repository;

    @Override
    public List<EmployeeStatusResponse> selectAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<EmployeeStatusResponse> selectApprovalStatuses() {
        return repository.findAll().stream()
                .filter(e -> APPROVAL_STATUSES.contains(e.getName()))
                .map(this::toResponse)
                .toList();
    }

    private EmployeeStatusResponse toResponse(EmployeeStatus e) {
        return EmployeeStatusResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .build();
    }
}
