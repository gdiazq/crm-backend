package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.BulkImportResult;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.SafetyComplianceRequest;
import com.crm.mcsv_rrhh.dto.SafetyComplianceResponse;
import com.crm.mcsv_rrhh.dto.UpdateSafetyComplianceRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface SafetyComplianceService {

    SafetyComplianceResponse create(SafetyComplianceRequest request);

    SafetyComplianceResponse update(UpdateSafetyComplianceRequest request);

    void updateStatus(Long id, Boolean active);

    SafetyComplianceResponse getById(Long id);

    PagedResponse<SafetyComplianceResponse> list(String search, Boolean active,
                                                  LocalDate createdFrom, LocalDate createdTo,
                                                  LocalDate updatedFrom, LocalDate updatedTo,
                                                  Pageable pageable);

    byte[] exportCsv();

    BulkImportResult importFromCsv(MultipartFile file);
}
