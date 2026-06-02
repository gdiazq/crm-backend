package com.crm.mcsv_rrhh.service.familyallowancetier;

import com.crm.mcsv_rrhh.dto.familyallowancetier.FamilyAllowanceTierResponse;

import java.util.List;

public interface FamilyAllowanceTierService {

    List<FamilyAllowanceTierResponse> selectAll();
}
