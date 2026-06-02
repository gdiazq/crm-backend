package com.crm.mcsv_rrhh.service.expat.impl;

import com.crm.mcsv_rrhh.dto.expat.ExpatResponse;
import com.crm.mcsv_rrhh.entity.expat.Expat;
import com.crm.mcsv_rrhh.repository.expat.ExpatRepository;
import com.crm.mcsv_rrhh.service.expat.ExpatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpatServiceImpl implements ExpatService {

    private final ExpatRepository repository;

    @Override
    public List<ExpatResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> ExpatResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
