package com.crm.mcsv_rrhh.service.healthinsurancetariff.impl;

import com.crm.mcsv_rrhh.dto.healthinsurancetariff.HealthInsuranceTariffResponse;
import com.crm.mcsv_rrhh.entity.healthinsurancetariff.HealthInsuranceTariff;
import com.crm.mcsv_rrhh.repository.healthinsurancetariff.HealthInsuranceTariffRepository;
import com.crm.mcsv_rrhh.service.healthinsurancetariff.HealthInsuranceTariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthInsuranceTariffServiceImpl implements HealthInsuranceTariffService {

    private final HealthInsuranceTariffRepository repository;

    @Override
    public List<HealthInsuranceTariffResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> HealthInsuranceTariffResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
