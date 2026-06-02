package com.crm.mcsv_rrhh.service.transporttype.impl;

import com.crm.mcsv_rrhh.dto.transporttype.TransportTypeResponse;
import com.crm.mcsv_rrhh.entity.transporttype.TransportType;
import com.crm.mcsv_rrhh.repository.transporttype.TransportTypeRepository;
import com.crm.mcsv_rrhh.service.transporttype.TransportTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportTypeServiceImpl implements TransportTypeService {

    private final TransportTypeRepository repository;

    @Override
    public List<TransportTypeResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> TransportTypeResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
