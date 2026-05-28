package com.crm.mcsv_rrhh.service.settlement;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.settlement.SettlementRequest;
import com.crm.mcsv_rrhh.dto.settlement.SettlementResponse;
import com.crm.mcsv_rrhh.dto.settlement.UpdateSettlementRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface SettlementService {

    PagedResponse<SettlementResponse> list(String search, String status,
                                            Long legalTerminationCauseId,
                                            Long qualityOfWorkId,
                                            Long safetyComplianceId,
                                            Long noReHiredCauseId,
                                            Boolean rehireEligible,
                                            LocalDate endDateFrom, LocalDate endDateTo,
                                            LocalDate createdFrom, LocalDate createdTo,
                                            Pageable pageable);

    SettlementResponse getById(Long id);

    SettlementResponse create(SettlementRequest request, List<MultipartFile> files);

    SettlementResponse update(UpdateSettlementRequest request, List<MultipartFile> files);

    byte[] exportCsv();
}
