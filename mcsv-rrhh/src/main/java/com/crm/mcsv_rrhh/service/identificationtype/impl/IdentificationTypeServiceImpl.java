package com.crm.mcsv_rrhh.service.identificationtype.impl;

import com.crm.mcsv_rrhh.dto.identificationtype.IdentificationTypeResponse;
import com.crm.mcsv_rrhh.entity.identificationtype.IdentificationType;
import com.crm.mcsv_rrhh.repository.identificationtype.IdentificationTypeRepository;
import com.crm.mcsv_rrhh.service.identificationtype.IdentificationTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IdentificationTypeServiceImpl implements IdentificationTypeService {

    private final IdentificationTypeRepository repository;

    @Override
    public List<IdentificationTypeResponse> selectActive() {
        return repository.findByStatusTrue().stream()
                .map(e -> IdentificationTypeResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
