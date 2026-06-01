package com.crm.mcsv_rrhh.service.contract;

import com.crm.mcsv_rrhh.dto.contract.ContractStatusResponse;

import java.util.List;

public interface ContractStatusService {

    List<ContractStatusResponse> selectAll();
}
