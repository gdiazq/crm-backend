package com.crm.mcsv_rrhh.service;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.EmployeeLeaveRequest;
import com.crm.mcsv_rrhh.dto.EmployeeLeaveResponse;
import com.crm.mcsv_rrhh.dto.UpdateEmployeeLeaveRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeLeaveService {
    PagedResponse<EmployeeLeaveResponse> list(String search, String status,
                                              Long leaveTypeId, Long employeeId, Long contractId,
                                              LocalDate startFrom, LocalDate startTo,
                                              LocalDate createdFrom, LocalDate createdTo,
                                              LocalDate updatedFrom, LocalDate updatedTo,
                                              Pageable pageable, String sortBy, String sortDir);
    EmployeeLeaveResponse getById(Long id);
    EmployeeLeaveResponse create(EmployeeLeaveRequest request, List<MultipartFile> files);
    EmployeeLeaveResponse update(UpdateEmployeeLeaveRequest request, List<MultipartFile> files);
    void deleteDocument(Long leaveId, Long fileId, Long userId);
    byte[] exportCsv();
    List<EmployeeLeaveResponse> findByEmployee(Long employeeId);
}
