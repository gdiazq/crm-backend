package com.crm.mcsv_project.service;

import com.crm.mcsv_project.dto.BulkImportResult;
import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectSpecialtyRequest;
import com.crm.mcsv_project.dto.ProjectSpecialtyResponse;
import com.crm.mcsv_project.dto.UpdateProjectSpecialtyRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProjectSpecialtyService {
    ProjectSpecialtyResponse create(ProjectSpecialtyRequest request);
    ProjectSpecialtyResponse update(UpdateProjectSpecialtyRequest request);
    void updateStatus(Long id, Boolean active);
    ProjectSpecialtyResponse getById(Long id);
    PagedResponse<ProjectSpecialtyResponse> list(String search, Boolean active, Pageable pageable);
    byte[] exportCsv();
    BulkImportResult importFromCsv(MultipartFile file);
}
