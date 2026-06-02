package com.crm.mcsv_rrhh.service.laborunion.impl;

import com.crm.mcsv_rrhh.dto.laborunion.LaborUnionResponse;
import com.crm.mcsv_rrhh.entity.laborunion.LaborUnion;
import com.crm.mcsv_rrhh.repository.laborunion.LaborUnionRepository;
import com.crm.mcsv_rrhh.service.laborunion.LaborUnionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LaborUnionServiceImpl implements LaborUnionService {

    private final LaborUnionRepository repository;

    @Override
    public List<LaborUnionResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> LaborUnionResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
