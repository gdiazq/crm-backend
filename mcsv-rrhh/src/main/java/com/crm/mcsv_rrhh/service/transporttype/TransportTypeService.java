package com.crm.mcsv_rrhh.service.transporttype;

import com.crm.mcsv_rrhh.dto.transporttype.TransportTypeResponse;

import java.util.List;

public interface TransportTypeService {

    List<TransportTypeResponse> selectAll();
}
