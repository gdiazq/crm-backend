package com.crm.mcsv_rrhh.service.contractannex.impl;

import com.crm.mcsv_rrhh.dto.contractannex.ContractAnnexTypeResponse;
import com.crm.mcsv_rrhh.entity.contractannex.ContractAnnexType;
import com.crm.mcsv_rrhh.repository.contractannex.ContractAnnexTypeRepository;
import com.crm.mcsv_rrhh.service.contractannex.ContractAnnexTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractAnnexTypeServiceImpl implements ContractAnnexTypeService {

    private final ContractAnnexTypeRepository repository;

    @Override
    public List<ContractAnnexTypeResponse> selectActive() {
        return repository.findByActiveTrue().stream()
                .map(e -> ContractAnnexTypeResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
