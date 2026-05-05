package com.crm.mcsv_rrhh.service;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.AttendanceRequest;
import com.crm.mcsv_rrhh.dto.AttendanceResponse;
import com.crm.mcsv_rrhh.dto.UpdateAttendanceRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    PagedResponse<AttendanceResponse> list(String search,
                                           Long employeeId,
                                           Integer costCenter,
                                           Long statusId,
                                           LocalDate dateFrom,
                                           LocalDate dateTo,
                                           LocalDate createdFrom,
                                           LocalDate createdTo,
                                           LocalDate updatedFrom,
                                           LocalDate updatedTo,
                                           Pageable pageable,
                                           String sortBy,
                                           String sortDir);

    AttendanceResponse getById(Long id);

    AttendanceResponse create(AttendanceRequest request);

    AttendanceResponse update(UpdateAttendanceRequest request);

    void delete(Long id);

    List<AttendanceResponse> findByEmployee(Long employeeId);

    List<AttendanceResponse> findByCostCenter(Integer costCenter);

    byte[] exportCsv(String search,
                     Long employeeId,
                     Integer costCenter,
                     Long statusId,
                     LocalDate dateFrom,
                     LocalDate dateTo);
}
