package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.AttendanceMarkRequest;
import com.crm.mcsv_rrhh.dto.AttendanceMarkResponse;
import com.crm.mcsv_rrhh.dto.AttendanceMarkTypeSelectItem;
import com.crm.mcsv_rrhh.dto.UpdateAttendanceMarkRequest;

import java.util.List;

public interface AttendanceMarkService {

    AttendanceMarkResponse create(AttendanceMarkRequest request);

    AttendanceMarkResponse update(UpdateAttendanceMarkRequest request);

    List<AttendanceMarkResponse> findByAttendance(Long attendanceId);

    List<AttendanceMarkTypeSelectItem> findMarkTypes();
}
