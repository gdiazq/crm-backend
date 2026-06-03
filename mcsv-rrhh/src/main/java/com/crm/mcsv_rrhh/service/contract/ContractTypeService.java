package com.crm.mcsv_rrhh.service.contract;

import com.crm.mcsv_rrhh.dto.contract.ContractTypeResponse;

import java.util.List;

public interface ContractTypeService {

    List<ContractTypeResponse> selectAll();

    ContractTypeResponse getById(Long id);
}
