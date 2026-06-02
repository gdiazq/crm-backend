package com.crm.mcsv_rrhh.service.city;

import com.crm.mcsv_rrhh.dto.city.CityResponse;

import java.util.List;

public interface CityService {

    List<CityResponse> select(Long communeId);
}
