package com.crm.mcsv_rrhh.service.settlement;

import com.crm.common.dto.BulkImportResult;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.settlement.NoReHiredCauseRequest;
import com.crm.mcsv_rrhh.dto.settlement.NoReHiredCauseResponse;
import com.crm.mcsv_rrhh.dto.settlement.NoReHiredCauseSelectResponse;
import com.crm.mcsv_rrhh.dto.settlement.UpdateNoReHiredCauseRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface NoReHiredCauseService {

    NoReHiredCauseResponse create(NoReHiredCauseRequest request);

    NoReHiredCauseResponse update(UpdateNoReHiredCauseRequest request);

    void updateStatus(Long id, Boolean active);

    NoReHiredCauseResponse getById(Long id);

    List<NoReHiredCauseSelectResponse> selectActive();

    PagedResponse<NoReHiredCauseResponse> list(String search, Boolean active,
                                               LocalDate createdFrom, LocalDate createdTo,
                                               LocalDate updatedFrom, LocalDate updatedTo,
                                               Pageable pageable);

    byte[] exportCsv();

    BulkImportResult importFromCsv(MultipartFile file);
}
