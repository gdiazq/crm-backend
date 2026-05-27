package com.crm.mcsv_project.service;

import com.crm.common.dto.BulkImportResult;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectRequest;
import com.crm.mcsv_project.dto.ProjectResponse;
import com.crm.mcsv_project.dto.UpdateProjectRequest;
import com.crm.mcsv_project.client.ContractResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface ProjectService {

    PagedResponse<ProjectResponse> list(String search, Boolean active,
                                        Long typeId, Long statusId, Long specialtyId,
                                        LocalDate createdFrom, LocalDate createdTo,
                                        LocalDate updatedFrom, LocalDate updatedTo,
                                        Pageable pageable);

    ProjectResponse getById(Long id);

    PagedResponse<ContractResponseDTO> listContractsByCostCenter(Integer costCenter, String search,
                                                                 Long contractStatusId, int page, int size,
                                                                 String sortBy, String sortDir);

    ProjectResponse create(ProjectRequest request);

    ProjectResponse update(UpdateProjectRequest request);

    void updateStatus(Long id, Boolean active);

    byte[] exportCsv();

    BulkImportResult importFromCsv(MultipartFile file);
}
