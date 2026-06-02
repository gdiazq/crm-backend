package com.crm.mcsv_rrhh.service.commune;

import com.crm.mcsv_rrhh.dto.commune.CommuneResponse;

import java.util.List;

public interface CommuneService {

    List<CommuneResponse> select(Long regionId);
}
