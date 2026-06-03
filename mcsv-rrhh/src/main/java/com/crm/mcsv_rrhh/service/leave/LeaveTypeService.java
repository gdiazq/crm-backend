package com.crm.mcsv_rrhh.service.leave;

import com.crm.mcsv_rrhh.dto.leave.LeaveTypeResponse;

import java.util.List;

public interface LeaveTypeService {

    List<LeaveTypeResponse> selectActive();
}
