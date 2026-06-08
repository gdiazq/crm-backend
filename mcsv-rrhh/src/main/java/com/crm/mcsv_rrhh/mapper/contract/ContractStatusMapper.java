package com.crm.mcsv_rrhh.mapper.contract;

import com.crm.mcsv_rrhh.dto.contract.ContractStatusResponse;
import com.crm.mcsv_rrhh.entity.contract.ContractStatus;
import org.springframework.stereotype.Component;

@Component
public class ContractStatusMapper {

    public ContractStatusResponse toResponse(ContractStatus status) {
        return ContractStatusResponse.builder()
                .id(status.getId())
                .name(status.getName())
                .build();
    }
}
