package com.crm.mcsv_rrhh.service.healthinsurance.impl;

import com.crm.mcsv_rrhh.dto.healthinsurance.HealthInsuranceResponse;
import com.crm.mcsv_rrhh.entity.healthinsurance.HealthInsurance;
import com.crm.mcsv_rrhh.repository.healthinsurance.HealthInsuranceRepository;
import com.crm.mcsv_rrhh.service.healthinsurance.HealthInsuranceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthInsuranceServiceImpl implements HealthInsuranceService {

    private final HealthInsuranceRepository repository;

    @Override
    public List<HealthInsuranceResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> HealthInsuranceResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
