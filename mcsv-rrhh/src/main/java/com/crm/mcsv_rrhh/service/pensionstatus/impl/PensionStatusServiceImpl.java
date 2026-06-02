package com.crm.mcsv_rrhh.service.pensionstatus.impl;

import com.crm.mcsv_rrhh.dto.pensionstatus.PensionStatusResponse;
import com.crm.mcsv_rrhh.entity.pensionstatus.PensionStatus;
import com.crm.mcsv_rrhh.repository.pensionstatus.PensionStatusRepository;
import com.crm.mcsv_rrhh.service.pensionstatus.PensionStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PensionStatusServiceImpl implements PensionStatusService {

    private final PensionStatusRepository repository;

    @Override
    public List<PensionStatusResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> PensionStatusResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
