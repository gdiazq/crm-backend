package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.CreateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.EmployeeResponse;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.UpdateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface EmployeeService {

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse getEmployeeByUserId(Long userId);

    Page<EmployeeResponse> filterEmployees(String search, Boolean active, Long statusId, Long companyId, Pageable pageable);

    Map<String, Long> getEmployeeStats();

    void updateStatus(Long id, Boolean active);

    void linkUser(Long id, Long userId);

    void unlinkUser(Long id);

    PagedResponse<UserDTO> getAvailableUsersForEmployee(String search, int page, int size);
}
