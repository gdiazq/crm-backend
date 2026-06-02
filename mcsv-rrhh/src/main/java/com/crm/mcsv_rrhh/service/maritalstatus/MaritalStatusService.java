package com.crm.mcsv_rrhh.service.maritalstatus;

import com.crm.mcsv_rrhh.dto.maritalstatus.MaritalStatusResponse;

import java.util.List;

public interface MaritalStatusService {

    List<MaritalStatusResponse> selectAll();
}
