package com.crm.mcsv_project.service;

import com.crm.common.dto.BulkImportResult;
import com.crm.mcsv_project.dto.ProjectTypeRequest;
import com.crm.mcsv_project.dto.ProjectTypeResponse;
import com.crm.mcsv_project.dto.UpdateProjectTypeRequest;
import com.crm.common.dto.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface ProjectTypeService {
    ProjectTypeResponse create(ProjectTypeRequest request);
    ProjectTypeResponse update(UpdateProjectTypeRequest request);
    void updateStatus(Long id, Boolean active);
    ProjectTypeResponse getById(Long id);
    PagedResponse<ProjectTypeResponse> list(String search, Boolean active, LocalDate createdFrom, LocalDate createdTo, LocalDate updatedFrom, LocalDate updatedTo, Pageable pageable);
    byte[] exportCsv();
    BulkImportResult importFromCsv(MultipartFile file);
}
