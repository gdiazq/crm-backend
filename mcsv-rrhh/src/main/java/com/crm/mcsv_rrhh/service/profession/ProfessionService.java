package com.crm.mcsv_rrhh.service.profession;

import com.crm.mcsv_rrhh.dto.profession.ProfessionResponse;

import java.util.List;

public interface ProfessionService {

    List<ProfessionResponse> selectAll();
}
