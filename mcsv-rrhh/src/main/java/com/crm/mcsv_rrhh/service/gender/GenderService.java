package com.crm.mcsv_rrhh.service.gender;

import com.crm.mcsv_rrhh.dto.gender.GenderResponse;

import java.util.List;

public interface GenderService {

    List<GenderResponse> selectAll();
}
