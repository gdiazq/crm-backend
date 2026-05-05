package com.crm.mcsv_rrhh.service;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.AttendanceStatusRequest;
import com.crm.mcsv_rrhh.dto.AttendanceStatusResponse;
import com.crm.mcsv_rrhh.dto.UpdateAttendanceStatusRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceStatusService {

    AttendanceStatusResponse create(AttendanceStatusRequest request);

    AttendanceStatusResponse update(UpdateAttendanceStatusRequest request);

    void updateStatus(Long id, Boolean active);

    AttendanceStatusResponse getById(Long id);

    PagedResponse<AttendanceStatusResponse> list(String search, Boolean active,
                                                  LocalDate createdFrom, LocalDate createdTo,
                                                  LocalDate updatedFrom, LocalDate updatedTo,
                                                  Pageable pageable);

    List<AttendanceStatusResponse> selectActive();
}
