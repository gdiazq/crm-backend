package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.TerminationAgreementRequest;
import com.crm.mcsv_rrhh.dto.TerminationAgreementResponse;
import com.crm.mcsv_rrhh.dto.UpdateTerminationAgreementRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TerminationAgreementService {

    PagedResponse<TerminationAgreementResponse> list(String search,
                                                      String status,
                                                      Long employeeId,
                                                      Long legalTerminationCauseId,
                                                      Boolean rehireEligible,
                                                      LocalDate endDateFrom,
                                                      LocalDate endDateTo,
                                                      LocalDate createdFrom,
                                                      LocalDate createdTo,
                                                      Pageable pageable);

    TerminationAgreementResponse getById(Long id);

    TerminationAgreementResponse create(TerminationAgreementRequest request);

    TerminationAgreementResponse update(UpdateTerminationAgreementRequest request);

    void sign(Long id);

    void cancel(Long id);

    byte[] exportCsv();
}
