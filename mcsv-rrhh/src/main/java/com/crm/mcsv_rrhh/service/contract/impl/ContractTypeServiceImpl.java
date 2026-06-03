package com.crm.mcsv_rrhh.service.contract.impl;

import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.dto.contract.ContractTypeResponse;
import com.crm.mcsv_rrhh.entity.contract.ContractType;
import com.crm.mcsv_rrhh.repository.contract.ContractTypeRepository;
import com.crm.mcsv_rrhh.service.contract.ContractTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractTypeServiceImpl implements ContractTypeService {

    private final ContractTypeRepository repository;

    @Override
    public List<ContractTypeResponse> selectAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ContractTypeResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de contrato no encontrado: " + id));
    }

    private ContractTypeResponse toResponse(ContractType e) {
        return ContractTypeResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .build();
    }
}
