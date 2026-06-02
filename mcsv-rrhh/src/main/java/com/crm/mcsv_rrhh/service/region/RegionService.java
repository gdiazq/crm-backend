package com.crm.mcsv_rrhh.service.region;

import com.crm.mcsv_rrhh.dto.region.RegionResponse;

import java.util.List;

public interface RegionService {

    List<RegionResponse> selectAll();
}
