package com.crm.mcsv_rrhh.service.expat;

import com.crm.mcsv_rrhh.dto.expat.ExpatResponse;

import java.util.List;

public interface ExpatService {

    List<ExpatResponse> selectAll();
}
