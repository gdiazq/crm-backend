package com.crm.mcsv_rrhh.service.hrrequest.impl;

import com.crm.mcsv_rrhh.dto.hrrequest.HRRequestTypeResponse;
import com.crm.mcsv_rrhh.entity.hrrequest.HRRequestType;
import com.crm.mcsv_rrhh.repository.hrrequest.HRRequestTypeRepository;
import com.crm.mcsv_rrhh.service.hrrequest.HRRequestTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HRRequestTypeServiceImpl implements HRRequestTypeService {

    private final HRRequestTypeRepository repository;

    @Override
    public List<HRRequestTypeResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> HRRequestTypeResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
