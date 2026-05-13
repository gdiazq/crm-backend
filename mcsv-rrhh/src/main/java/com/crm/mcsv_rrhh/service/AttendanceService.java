package com.crm.mcsv_rrhh.service;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.AttendanceResponse;
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

    List<AttendanceResponse> findByCostCenter(Integer costCenter);

    byte[] exportCsv(String search,
                     Long employeeId,
                     Integer costCenter,
                     Long statusId,
                     LocalDate dateFrom,
                     LocalDate dateTo);
}
