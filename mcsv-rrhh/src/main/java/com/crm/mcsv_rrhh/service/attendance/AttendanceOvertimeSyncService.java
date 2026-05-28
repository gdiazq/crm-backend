package com.crm.mcsv_rrhh.service.attendance;

import com.crm.mcsv_rrhh.entity.overtime.Overtime;

public interface AttendanceOvertimeSyncService {

    void recalculateAttendanceOvertime(Long attendanceId);

    void applyApprovedOvertime(Overtime overtime);

    void revertOvertime(Overtime overtime);
}
