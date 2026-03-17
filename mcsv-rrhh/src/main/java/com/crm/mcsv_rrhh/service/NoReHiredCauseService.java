package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.BulkImportResult;
import com.crm.mcsv_rrhh.dto.NoReHiredCauseRequest;
import com.crm.mcsv_rrhh.dto.NoReHiredCauseResponse;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.UpdateNoReHiredCauseRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface NoReHiredCauseService {

    NoReHiredCauseResponse create(NoReHiredCauseRequest request);

    NoReHiredCauseResponse update(UpdateNoReHiredCauseRequest request);

    void updateStatus(Long id, Boolean active);

    NoReHiredCauseResponse getById(Long id);

    PagedResponse<NoReHiredCauseResponse> list(String search, Boolean active,
                                               LocalDate createdFrom, LocalDate createdTo,
                                               LocalDate updatedFrom, LocalDate updatedTo,
                                               Pageable pageable);

    byte[] exportCsv();

    BulkImportResult importFromCsv(MultipartFile file);
}
