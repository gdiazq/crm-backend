package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.AttendanceStatusResponse;

import java.util.List;

public interface AttendanceStatusService {

    List<AttendanceStatusResponse> selectActive();
}
