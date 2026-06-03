package com.crm.mcsv_rrhh.service.employee;

import com.crm.mcsv_rrhh.dto.employee.EmployeeStatusResponse;

import java.util.List;

public interface EmployeeStatusService {

    List<EmployeeStatusResponse> selectAll();

    List<EmployeeStatusResponse> selectApprovalStatuses();
}
