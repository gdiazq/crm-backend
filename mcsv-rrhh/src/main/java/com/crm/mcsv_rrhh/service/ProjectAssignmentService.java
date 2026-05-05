package com.crm.mcsv_rrhh.service;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.ProjectAssignmentResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ProjectAssignmentService {

    PagedResponse<ProjectAssignmentResponse> list(String search,
                                                  Long employeeId,
                                                  Integer costCenter,
                                                  Boolean active,
                                                  LocalDate dateFrom,
                                                  LocalDate dateTo,
                                                  LocalDate createdFrom,
                                                  LocalDate createdTo,
                                                  LocalDate updatedFrom,
                                                  LocalDate updatedTo,
                                                  Pageable pageable,
                                                  String sortBy,
                                                  String sortDir);

    ProjectAssignmentResponse getById(Long id);

    List<ProjectAssignmentResponse> findByEmployee(Long employeeId);

    List<ProjectAssignmentResponse> findByCostCenter(Integer costCenter);
}
