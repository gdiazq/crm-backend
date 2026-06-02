package com.crm.mcsv_rrhh.service.nationality;

import com.crm.mcsv_rrhh.dto.nationality.NationalityResponse;

import java.util.List;

public interface NationalityService {

    List<NationalityResponse> selectAll();
}
