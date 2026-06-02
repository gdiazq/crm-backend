package com.crm.mcsv_rrhh.service.safetygroup;

import com.crm.mcsv_rrhh.dto.safetygroup.SafetyGroupResponse;

import java.util.List;

public interface SafetyGroupService {

    List<SafetyGroupResponse> selectAll();
}
