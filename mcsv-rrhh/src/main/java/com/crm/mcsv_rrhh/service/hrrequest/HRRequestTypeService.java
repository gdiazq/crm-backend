package com.crm.mcsv_rrhh.service.hrrequest;

import com.crm.mcsv_rrhh.dto.hrrequest.HRRequestTypeResponse;

import java.util.List;

public interface HRRequestTypeService {

    List<HRRequestTypeResponse> selectAll();
}
