package com.crm.mcsv_rrhh.service.pensionstatus;

import com.crm.mcsv_rrhh.dto.pensionstatus.PensionStatusResponse;

import java.util.List;

public interface PensionStatusService {

    List<PensionStatusResponse> selectAll();
}
