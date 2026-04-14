package com.crm.mcsv_rrhh.service;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.TerminationQuizQuestionRequest;
import com.crm.mcsv_rrhh.dto.TerminationQuizQuestionResponse;
import com.crm.mcsv_rrhh.dto.UpdateTerminationQuizQuestionRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TerminationQuizQuestionService {

    PagedResponse<TerminationQuizQuestionResponse> list(String search, Boolean active, String questionGroup,
                                                         Long employeeId,
                                                         LocalDate createdFrom, LocalDate createdTo,
                                                         LocalDate updatedFrom, LocalDate updatedTo,
                                                         Pageable pageable);

    TerminationQuizQuestionResponse getById(Long id);

    TerminationQuizQuestionResponse create(TerminationQuizQuestionRequest request);

    TerminationQuizQuestionResponse update(UpdateTerminationQuizQuestionRequest request);

    void updateStatus(Long id, Boolean active);

    List<TerminationQuizQuestionResponse> getActiveQuestions();
}
