package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface EmployeeService {

    EmployeeDetailResponse createEmployee(CreateEmployeeRequest request);

    EmployeeDetailResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    EmployeeDetailResponse getEmployeeById(Long id);

    EmployeeDetailResponse getEmployeeByUserId(Long userId);

    Page<EmployeeResponse> filterEmployees(String search, Boolean active, Long statusId,
                                           java.time.LocalDate createdFrom, java.time.LocalDate createdTo,
                                           Pageable pageable);

    Map<String, Long> getEmployeeStats();
    byte[] exportCsv();
    BulkImportResult importFromCsv(MultipartFile file);

    void updateStatus(Long id, Boolean active);

    void updateLinkedUser(Long id, Long userId);

    List<CatalogItem> getAvailableUsersForEmployee(String search);

    List<EmployeeSelectItem> getEmployeesWithoutContract();

    List<EmployeeSelectItem> getSupervisors();

    List<EmployeeSelectItem> getVisitors();

    List<EmployeeSelectItem> getCompanyRepresentatives();

    record EmployeeSelectItem(Long id, String name) {}
}
