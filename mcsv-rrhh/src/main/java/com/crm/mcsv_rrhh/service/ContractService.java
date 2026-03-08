package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.ContractDetailResponse;
import com.crm.mcsv_rrhh.dto.ContractResponse;
import com.crm.mcsv_rrhh.dto.CreateContractRequest;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface ContractService {
    ContractDetailResponse createContract(CreateContractRequest request);

    Page<ContractResponse> list(Long employeeId, Long statusId,
                                LocalDate createdFrom, LocalDate createdTo,
                                Pageable pageable);

    Map<String, Long> getStats(Long employeeId);
}
