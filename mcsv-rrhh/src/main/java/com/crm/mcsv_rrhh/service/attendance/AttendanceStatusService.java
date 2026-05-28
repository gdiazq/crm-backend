package com.crm.mcsv_rrhh.service.attendance;

import com.crm.mcsv_rrhh.dto.attendance.AttendanceStatusResponse;

import java.util.List;

public interface AttendanceStatusService {

    List<AttendanceStatusResponse> selectActive();
}
