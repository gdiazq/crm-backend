package com.crm.mcsv_rrhh.mapper.contract;

import com.crm.mcsv_rrhh.dto.contract.ContractTypeResponse;
import com.crm.mcsv_rrhh.entity.contract.ContractType;
import org.springframework.stereotype.Component;

@Component
public class ContractTypeMapper {

    public ContractTypeResponse toResponse(ContractType type) {
        return ContractTypeResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .build();
    }
}
