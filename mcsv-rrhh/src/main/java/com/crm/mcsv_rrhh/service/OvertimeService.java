package com.crm.mcsv_rrhh.service;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.OvertimeRequest;
import com.crm.mcsv_rrhh.dto.OvertimeResponse;
import com.crm.mcsv_rrhh.dto.OvertimeTypeResponse;
import com.crm.mcsv_rrhh.dto.OvertimeUpdateRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface OvertimeService {

    PagedResponse<OvertimeResponse> list(String search,
                                         Long employeeId, Integer costCenter, Long statusId,
                                         LocalDate dateFrom, LocalDate dateTo,
                                         Long overtimeTypeId, Pageable pageable);

    OvertimeResponse getById(Long id);

    OvertimeResponse create(OvertimeRequest request, Long userId);

    OvertimeResponse update(OvertimeUpdateRequest request, Long userId);

    List<OvertimeTypeResponse> listTypes();
}
