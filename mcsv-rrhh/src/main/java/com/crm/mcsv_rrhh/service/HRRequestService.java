package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.HRRequestResponse;
import com.crm.mcsv_rrhh.dto.RejectHRRequestRequest;
import com.crm.mcsv_rrhh.entity.HRRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HRRequestService {
    HRRequest createForEmployee(Long employeeId, String requestTypeName);
    Page<HRRequestResponse> list(Long idModule, Pageable pageable);
    HRRequestResponse getById(Long id);
    HRRequestResponse approve(Long id, Long approverId);
    HRRequestResponse reject(Long id, RejectHRRequestRequest req);
}
