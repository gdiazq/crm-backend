package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.BulkImportResult;
import com.crm.mcsv_rrhh.dto.CreateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.EmployeeDetailResponse;
import com.crm.mcsv_rrhh.dto.EmployeeResponse;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.UpdateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.UserDTO;
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

    void linkUser(Long id, Long userId);

    void unlinkUser(Long id);

    PagedResponse<UserDTO> getAvailableUsersForEmployee(String search, int page, int size);

    List<EmployeeSelectItem> getEmployeesWithoutContract();

    record EmployeeSelectItem(Long id, String name) {}
}
