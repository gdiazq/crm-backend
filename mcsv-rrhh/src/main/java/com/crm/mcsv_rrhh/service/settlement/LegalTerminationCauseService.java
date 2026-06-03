package com.crm.mcsv_rrhh.service.settlement;

import com.crm.common.dto.BulkImportResult;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.settlement.LegalTerminationCauseRequest;
import com.crm.mcsv_rrhh.dto.settlement.LegalTerminationCauseResponse;
import com.crm.mcsv_rrhh.dto.settlement.LegalTerminationCauseSelectResponse;
import com.crm.mcsv_rrhh.dto.settlement.UpdateLegalTerminationCauseRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface LegalTerminationCauseService {

    LegalTerminationCauseResponse create(LegalTerminationCauseRequest request);

    LegalTerminationCauseResponse update(UpdateLegalTerminationCauseRequest request);

    void updateStatus(Long id, Boolean active);

    LegalTerminationCauseResponse getById(Long id);

    List<LegalTerminationCauseSelectResponse> selectActive();

    PagedResponse<LegalTerminationCauseResponse> list(String search, Boolean active,
                                                       LocalDate createdFrom, LocalDate createdTo,
                                                       LocalDate updatedFrom, LocalDate updatedTo,
                                                       Pageable pageable);

    byte[] exportCsv();

    BulkImportResult importFromCsv(MultipartFile file);
}
