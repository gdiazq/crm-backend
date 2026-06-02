package com.crm.mcsv_rrhh.service.healthinsurancetariff;

import com.crm.mcsv_rrhh.dto.healthinsurancetariff.HealthInsuranceTariffResponse;

import java.util.List;

public interface HealthInsuranceTariffService {

    List<HealthInsuranceTariffResponse> selectAll();
}
