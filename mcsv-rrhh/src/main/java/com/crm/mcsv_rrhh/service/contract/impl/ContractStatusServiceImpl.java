package com.crm.mcsv_rrhh.service.contract.impl;

import com.crm.mcsv_rrhh.dto.contract.ContractStatusResponse;
import com.crm.mcsv_rrhh.mapper.contract.ContractStatusMapper;
import com.crm.mcsv_rrhh.repository.contract.ContractStatusRepository;
import com.crm.mcsv_rrhh.service.contract.ContractStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractStatusServiceImpl implements ContractStatusService {

    private final ContractStatusRepository repository;
    private final ContractStatusMapper mapper;

    @Override
    public List<ContractStatusResponse> selectAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }
}
