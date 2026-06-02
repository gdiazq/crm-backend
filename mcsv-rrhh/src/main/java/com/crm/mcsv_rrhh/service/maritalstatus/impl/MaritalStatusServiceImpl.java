package com.crm.mcsv_rrhh.service.maritalstatus.impl;

import com.crm.mcsv_rrhh.dto.maritalstatus.MaritalStatusResponse;
import com.crm.mcsv_rrhh.entity.maritalstatus.MaritalStatus;
import com.crm.mcsv_rrhh.repository.maritalstatus.MaritalStatusRepository;
import com.crm.mcsv_rrhh.service.maritalstatus.MaritalStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaritalStatusServiceImpl implements MaritalStatusService {

    private final MaritalStatusRepository repository;

    @Override
    public List<MaritalStatusResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> MaritalStatusResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
