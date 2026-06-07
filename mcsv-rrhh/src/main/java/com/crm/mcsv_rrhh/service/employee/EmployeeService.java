package com.crm.mcsv_rrhh.service.employee;

import com.crm.common.dto.BulkImportResult;
import com.crm.common.dto.PagedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.employee.CreateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.employee.EmployeeDetailResponse;
import com.crm.mcsv_rrhh.dto.employee.EmployeeResponse;
import com.crm.mcsv_rrhh.dto.employee.UpdateEmployeeRequest;

public interface EmployeeService {

    EmployeeDetailResponse createEmployee(CreateEmployeeRequest request);

    EmployeeDetailResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    EmployeeDetailResponse getEmployeeById(Long id);

    EmployeeDetailResponse getEmployeeByUserId(Long userId);

    PagedResponse<EmployeeResponse> listEmployees(String search, Boolean active, Long statusId,
                                                  LocalDate createdFrom, LocalDate createdTo,
                                                  int page, int size, String sortBy, String sortDir);

    byte[] exportCsv();

    BulkImportResult importFromCsv(MultipartFile file);

    void updateStatus(Long id, Boolean active);

    void updateLinkedUser(Long id, Long userId);

    List<CatalogItem> getAvailableUsersForEmployee(String search);

    List<EmployeeSelectItem> getEmployeesWithoutContract();

    List<EmployeeSelectItem> getEmployeesWithContract();

    record EmployeeSelectItem(Long id, String name) {}
}
