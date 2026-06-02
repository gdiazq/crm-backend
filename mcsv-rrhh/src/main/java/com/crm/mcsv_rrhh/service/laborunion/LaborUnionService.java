package com.crm.mcsv_rrhh.service.laborunion;

import com.crm.mcsv_rrhh.dto.laborunion.LaborUnionResponse;

import java.util.List;

public interface LaborUnionService {

    List<LaborUnionResponse> selectAll();
}
