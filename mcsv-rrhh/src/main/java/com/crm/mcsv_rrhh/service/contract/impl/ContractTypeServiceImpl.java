package com.crm.mcsv_rrhh.service.contract.impl;

import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.dto.contract.ContractTypeResponse;
import com.crm.mcsv_rrhh.mapper.contract.ContractTypeMapper;
import com.crm.mcsv_rrhh.repository.contract.ContractTypeRepository;
import com.crm.mcsv_rrhh.service.contract.ContractTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractTypeServiceImpl implements ContractTypeService {

    private final ContractTypeRepository contractTypeRepository;
    private final ContractTypeMapper contractTypeMapper;

    @Override
    public List<ContractTypeResponse> selectAll() {
        return contractTypeRepository.findAll().stream()
                .map(contractTypeMapper::toResponse)
                .toList();
    }

    @Override
    public ContractTypeResponse getById(Long id) {
        return contractTypeRepository.findById(id)
                .map(contractTypeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de contrato no encontrado: " + id));
    }
}
