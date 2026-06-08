package com.crm.mcsv_rrhh.mapper.hrrequest;

import com.crm.mcsv_rrhh.dto.hrrequest.HRRequestTypeResponse;
import com.crm.mcsv_rrhh.entity.hrrequest.HRRequestType;
import org.springframework.stereotype.Component;

@Component
public class HRRequestTypeMapper {

    public HRRequestTypeResponse toResponse(HRRequestType type) {
        return HRRequestTypeResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .build();
    }
}
