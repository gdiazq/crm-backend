package com.crm.mcsv_project.service;

import com.crm.mcsv_project.dto.BulkImportResult;
import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectStatusRequest;
import com.crm.mcsv_project.dto.ProjectStatusResponse;
import com.crm.mcsv_project.dto.UpdateProjectStatusRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProjectStatusService {
    ProjectStatusResponse create(ProjectStatusRequest request);
    ProjectStatusResponse update(UpdateProjectStatusRequest request);
    void updateStatus(Long id, Boolean active);
    ProjectStatusResponse getById(Long id);
    PagedResponse<ProjectStatusResponse> list(String search, Boolean active, Pageable pageable);
    byte[] exportCsv();
    BulkImportResult importFromCsv(MultipartFile file);
}
