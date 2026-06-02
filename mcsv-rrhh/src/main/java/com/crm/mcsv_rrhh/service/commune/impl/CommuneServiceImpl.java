package com.crm.mcsv_rrhh.service.commune.impl;

import com.crm.mcsv_rrhh.dto.commune.CommuneResponse;
import com.crm.mcsv_rrhh.entity.commune.Commune;
import com.crm.mcsv_rrhh.repository.commune.CommuneRepository;
import com.crm.mcsv_rrhh.service.commune.CommuneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommuneServiceImpl implements CommuneService {

    private final CommuneRepository repository;

    @Override
    public List<CommuneResponse> select(Long regionId) {
        List<Commune> communes = regionId != null
                ? repository.findByRegionId(regionId)
                : repository.findAll();
        return communes.stream()
                .map(c -> CommuneResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();
    }
}
