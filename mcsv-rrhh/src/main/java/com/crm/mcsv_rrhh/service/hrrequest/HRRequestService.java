package com.crm.mcsv_rrhh.service.hrrequest;

import com.crm.mcsv_rrhh.dto.hrrequest.HRRequestDetailResponse;
import com.crm.mcsv_rrhh.dto.hrrequest.HRRequestResponse;
import com.crm.mcsv_rrhh.dto.hrrequest.RejectHRRequestRequest;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.entity.hrrequest.HRRequest;

import java.time.LocalDate;

public interface HRRequestService {

    HRRequest createForEmployee(Long employeeId, String requestTypeName, String action, String proposedData);

    HRRequest createForContract(Long contractId, Long employeeId, String action, String proposedData);

    HRRequest createForSettlement(Long settlementId, Long employeeId, String action, String proposedData);

    HRRequest createForTransfer(Long transferId, Long employeeId, String action, String proposedData);

    HRRequest createForAnnex(Long annexId, Long employeeId, String action, String proposedData);

    HRRequest createForLeave(Long leaveId, Long employeeId, String action, String proposedData);

    HRRequest createForOvertime(Long overtimeId, Long employeeId, String action, String proposedData);

    PagedResponse<HRRequestResponse> listRequests(Long idModule, Long statusId,
                                                  LocalDate createdFrom, LocalDate createdTo,
                                                  LocalDate approvalFrom, LocalDate approvalTo,
                                                  int page, int size, String sortBy, String sortDir);

    HRRequestDetailResponse getById(Long id);

    HRRequestResponse approve(Long id, Long approverId);

    byte[] exportCsv();

    HRRequestResponse reject(Long id, RejectHRRequestRequest req);

}
