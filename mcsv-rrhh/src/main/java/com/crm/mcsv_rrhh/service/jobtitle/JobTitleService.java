package com.crm.mcsv_rrhh.service.jobtitle;

import com.crm.mcsv_rrhh.dto.jobtitle.JobTitleResponse;

import java.util.List;

public interface JobTitleService {

    List<JobTitleResponse> selectAll();

    JobTitleResponse getById(Long id);
}
