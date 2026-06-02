package com.crm.mcsv_rrhh.service.region.impl;

import com.crm.mcsv_rrhh.dto.region.RegionResponse;
import com.crm.mcsv_rrhh.entity.region.Region;
import com.crm.mcsv_rrhh.repository.region.RegionRepository;
import com.crm.mcsv_rrhh.service.region.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionRepository repository;

    @Override
    public List<RegionResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> RegionResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
