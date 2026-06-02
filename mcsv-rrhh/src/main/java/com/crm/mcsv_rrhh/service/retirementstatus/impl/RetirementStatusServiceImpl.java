package com.crm.mcsv_rrhh.service.retirementstatus.impl;

import com.crm.mcsv_rrhh.dto.retirementstatus.RetirementStatusResponse;
import com.crm.mcsv_rrhh.entity.retirementstatus.RetirementStatus;
import com.crm.mcsv_rrhh.repository.retirementstatus.RetirementStatusRepository;
import com.crm.mcsv_rrhh.service.retirementstatus.RetirementStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetirementStatusServiceImpl implements RetirementStatusService {

    private final RetirementStatusRepository repository;

    @Override
    public List<RetirementStatusResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> RetirementStatusResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
