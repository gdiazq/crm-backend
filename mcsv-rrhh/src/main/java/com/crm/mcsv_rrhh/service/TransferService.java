package com.crm.mcsv_rrhh.service;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.TransferRequest;
import com.crm.mcsv_rrhh.dto.TransferResponse;
import com.crm.mcsv_rrhh.dto.UpdateTransferRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TransferService {
    PagedResponse<TransferResponse> list(String search, String status, Pageable pageable);
    TransferResponse getById(Long id);
    TransferResponse create(TransferRequest request, List<MultipartFile> files);
    TransferResponse update(UpdateTransferRequest request, List<MultipartFile> files);
    void deleteDocument(Long transferId, Long fileId, Long userId);
    byte[] exportCsv();
}
