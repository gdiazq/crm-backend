package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.ContractDetailResponse;
import com.crm.mcsv_rrhh.dto.CreateContractRequest;

public interface ContractService {
    ContractDetailResponse createContract(CreateContractRequest request);
}
