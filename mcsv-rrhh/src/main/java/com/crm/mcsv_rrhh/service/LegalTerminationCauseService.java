package com.crm.mcsv_rrhh.service;

import com.crm.common.dto.BulkImportResult;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.LegalTerminationCauseRequest;
import com.crm.mcsv_rrhh.dto.LegalTerminationCauseResponse;
import com.crm.mcsv_rrhh.dto.UpdateLegalTerminationCauseRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface LegalTerminationCauseService {

    LegalTerminationCauseResponse create(LegalTerminationCauseRequest request);

    LegalTerminationCauseResponse update(UpdateLegalTerminationCauseRequest request);

    void updateStatus(Long id, Boolean active);

    LegalTerminationCauseResponse getById(Long id);

    PagedResponse<LegalTerminationCauseResponse> list(String search, Boolean active,
                                                       LocalDate createdFrom, LocalDate createdTo,
                                                       LocalDate updatedFrom, LocalDate updatedTo,
                                                       Pageable pageable);

    byte[] exportCsv();

    BulkImportResult importFromCsv(MultipartFile file);
}
