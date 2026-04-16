package com.crm.mcsv_rrhh.service;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.TransferRequest;
import com.crm.mcsv_rrhh.dto.TransferResponse;
import com.crm.mcsv_rrhh.dto.UpdateTransferRequest;

import org.springframework.data.domain.Pageable;

public interface TransferService {
    PagedResponse<TransferResponse> list(Long employeeId, String status, Pageable pageable);
    TransferResponse getById(Long id);
    TransferResponse create(TransferRequest request);
    TransferResponse update(UpdateTransferRequest request);
    byte[] exportCsv();
}
