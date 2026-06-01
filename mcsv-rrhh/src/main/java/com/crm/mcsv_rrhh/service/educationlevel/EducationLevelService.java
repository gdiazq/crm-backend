package com.crm.mcsv_rrhh.service.educationlevel;

import com.crm.mcsv_rrhh.dto.educationlevel.EducationLevelResponse;

import java.util.List;

public interface EducationLevelService {

    List<EducationLevelResponse> selectAll();
}
