package com.crm.mcsv_rrhh.service.retirementstatus;

import com.crm.mcsv_rrhh.dto.retirementstatus.RetirementStatusResponse;

import java.util.List;

public interface RetirementStatusService {

    List<RetirementStatusResponse> selectAll();
}
