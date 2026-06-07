package com.crm.mcsv_rrhh.mapper.hrrequest;

import com.crm.mcsv_rrhh.entity.hrrequest.HRRequest;
import com.crm.mcsv_rrhh.entity.hrrequest.HRRequestType;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;


@Component
public class HRRequestMapper {

    public HRRequest toEntity(HRRequestType type, Long statusId, Long employeeId, String action,
                              String proposedData, Consumer<HRRequest.HRRequestBuilder> link) {
        HRRequest.HRRequestBuilder builder = HRRequest.builder()
                .requestTypeId(type.getId())
                .statusId(statusId)
                .requireApproval(type.getRequireApproval())
                .idModule(employeeId)
                .action(action)
                .proposedData(proposedData);
        if (link != null) link.accept(builder);
        return builder.build();
    }
}
