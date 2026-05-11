package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.entity.Overtime;

public interface AttendanceOvertimeSyncService {

    void recalculateAttendanceOvertime(Long attendanceId);

    void applyApprovedOvertime(Overtime overtime);

    void revertOvertime(Overtime overtime);
}
