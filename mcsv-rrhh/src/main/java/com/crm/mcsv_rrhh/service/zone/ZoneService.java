package com.crm.mcsv_rrhh.service.zone;

import com.crm.mcsv_rrhh.dto.zone.ZoneResponse;

import java.util.List;

public interface ZoneService {

    List<ZoneResponse> selectAll();
}
