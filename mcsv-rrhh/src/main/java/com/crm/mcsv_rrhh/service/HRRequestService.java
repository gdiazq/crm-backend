package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.HRRequestDetailResponse;
import com.crm.mcsv_rrhh.dto.HRRequestResponse;
import com.crm.mcsv_rrhh.dto.RejectHRRequestRequest;
import com.crm.mcsv_rrhh.entity.HRRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface HRRequestService {
    HRRequest createForEmployee(Long employeeId, String requestTypeName, String action, String proposedData);
    Page<HRRequestResponse> list(Long idModule, Long statusId,
                                  LocalDate createdFrom, LocalDate createdTo,
                                  LocalDate approvalFrom, LocalDate approvalTo,
                                  Pageable pageable);
    HRRequestDetailResponse getById(Long id);
    HRRequestResponse approve(Long id, Long approverId);
    byte[] exportCsv();
    HRRequestResponse reject(Long id, RejectHRRequestRequest req);
    Map<String, Long> getStats(Long idModule);
}
