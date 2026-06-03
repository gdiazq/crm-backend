package com.crm.mcsv_rrhh.service.identificationtype;

import com.crm.mcsv_rrhh.dto.identificationtype.IdentificationTypeResponse;

import java.util.List;

public interface IdentificationTypeService {

    List<IdentificationTypeResponse> selectActive();
}
