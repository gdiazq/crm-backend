package com.crm.mcsv_rrhh.service.driverlicense.impl;

import com.crm.mcsv_rrhh.dto.driverlicense.DriverLicenseResponse;
import com.crm.mcsv_rrhh.entity.driverlicense.DriverLicense;
import com.crm.mcsv_rrhh.repository.driverlicense.DriverLicenseRepository;
import com.crm.mcsv_rrhh.service.driverlicense.DriverLicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverLicenseServiceImpl implements DriverLicenseService {

    private final DriverLicenseRepository repository;

    @Override
    public List<DriverLicenseResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> DriverLicenseResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
