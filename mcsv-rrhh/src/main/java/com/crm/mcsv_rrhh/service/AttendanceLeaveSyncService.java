package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.entity.EmployeeLeave;

public interface AttendanceLeaveSyncService {

    void generateForApprovedLeave(EmployeeLeave leave);

    void revertGeneratedForLeave(Long leaveId);
}
