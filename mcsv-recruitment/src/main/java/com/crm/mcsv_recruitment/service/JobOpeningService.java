package com.crm.mcsv_recruitment.service;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_recruitment.dto.JobOpeningRequest;
import com.crm.mcsv_recruitment.dto.JobOpeningResponse;
import com.crm.mcsv_recruitment.dto.UpdateJobOpeningRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface JobOpeningService {

    PagedResponse<JobOpeningResponse> list(String search,
                                           Long statusId,
                                           Integer costCenter,
                                           Long supervisorUserId,
                                           LocalDate closeDateFrom,
                                           LocalDate closeDateTo,
                                           LocalDateTime createdFrom,
                                           LocalDateTime createdTo,
                                           Pageable pageable);

    JobOpeningResponse getById(Long id);

    JobOpeningResponse create(JobOpeningRequest request, Long currentUserId);

    JobOpeningResponse update(UpdateJobOpeningRequest request, Long currentUserId);

    void delete(Long id);
}
