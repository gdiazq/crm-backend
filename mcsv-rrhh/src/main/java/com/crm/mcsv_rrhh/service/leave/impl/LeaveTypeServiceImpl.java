package com.crm.mcsv_rrhh.service.leave.impl;

import com.crm.mcsv_rrhh.dto.leave.LeaveTypeResponse;
import com.crm.mcsv_rrhh.entity.leave.LeaveType;
import com.crm.mcsv_rrhh.repository.leave.LeaveTypeRepository;
import com.crm.mcsv_rrhh.service.leave.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository repository;

    @Override
    public List<LeaveTypeResponse> selectActive() {
        return repository.findByActiveTrue().stream()
                .map(e -> LeaveTypeResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
