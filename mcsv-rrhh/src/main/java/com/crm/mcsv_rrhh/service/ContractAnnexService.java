package com.crm.mcsv_rrhh.service;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.ContractAnnexRequest;
import com.crm.mcsv_rrhh.dto.ContractAnnexResponse;
import com.crm.mcsv_rrhh.dto.UpdateContractAnnexRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface ContractAnnexService {
    PagedResponse<ContractAnnexResponse> list(String search, String status,
                                               Long annexTypeId, Long contractId,
                                               LocalDate dateFrom, LocalDate dateTo,
                                               LocalDate createdFrom, LocalDate createdTo,
                                               LocalDate updatedFrom, LocalDate updatedTo,
                                               Pageable pageable, String sortBy, String sortDir);
    ContractAnnexResponse getById(Long id);
    ContractAnnexResponse create(ContractAnnexRequest request, List<MultipartFile> files);
    ContractAnnexResponse update(UpdateContractAnnexRequest request, List<MultipartFile> files);
    void deleteDocument(Long annexId, Long fileId, Long userId);
    byte[] exportCsv();
    List<ContractAnnexResponse> findByContract(Long contractId);
}
