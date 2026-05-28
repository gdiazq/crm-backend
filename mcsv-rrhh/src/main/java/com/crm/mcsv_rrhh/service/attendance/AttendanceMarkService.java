package com.crm.mcsv_rrhh.service.attendance;

import com.crm.mcsv_rrhh.dto.attendance.AttendanceMarkRequest;
import com.crm.mcsv_rrhh.dto.attendance.AttendanceMarkResponse;
import com.crm.mcsv_rrhh.dto.attendance.AttendanceMarkTypeSelectItem;
import com.crm.mcsv_rrhh.dto.attendance.UpdateAttendanceMarkRequest;

import java.util.List;

public interface AttendanceMarkService {

    AttendanceMarkResponse create(AttendanceMarkRequest request);

    AttendanceMarkResponse update(UpdateAttendanceMarkRequest request);

    List<AttendanceMarkResponse> findByAttendance(Long attendanceId);

    List<AttendanceMarkTypeSelectItem> findMarkTypes();
}
