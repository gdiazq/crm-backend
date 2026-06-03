package com.crm.mcsv_rrhh.service.jobtitle.impl;

import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.dto.jobtitle.JobTitleResponse;
import com.crm.mcsv_rrhh.entity.jobtitle.JobTitle;
import com.crm.mcsv_rrhh.repository.jobtitle.JobTitleRepository;
import com.crm.mcsv_rrhh.service.jobtitle.JobTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobTitleServiceImpl implements JobTitleService {

    private final JobTitleRepository repository;

    @Override
    public List<JobTitleResponse> selectAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public JobTitleResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado: " + id));
    }

    private JobTitleResponse toResponse(JobTitle e) {
        return JobTitleResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .build();
    }
}
