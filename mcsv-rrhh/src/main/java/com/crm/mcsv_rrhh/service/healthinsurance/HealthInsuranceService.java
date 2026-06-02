package com.crm.mcsv_rrhh.service.healthinsurance;

import com.crm.mcsv_rrhh.dto.healthinsurance.HealthInsuranceResponse;

import java.util.List;

public interface HealthInsuranceService {

    List<HealthInsuranceResponse> selectAll();
}
