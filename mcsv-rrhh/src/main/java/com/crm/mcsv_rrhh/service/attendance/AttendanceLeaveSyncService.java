package com.crm.mcsv_rrhh.service.attendance;

import com.crm.mcsv_rrhh.entity.leave.EmployeeLeave;

public interface AttendanceLeaveSyncService {

    void generateForApprovedLeave(EmployeeLeave leave);

    void revertGeneratedForLeave(Long leaveId);
}
