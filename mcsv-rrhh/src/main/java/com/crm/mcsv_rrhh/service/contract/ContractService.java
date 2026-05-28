package com.crm.mcsv_rrhh.service.contract;

import com.crm.common.dto.BulkImportResult;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.contract.ContractDetailResponse;
import com.crm.mcsv_rrhh.dto.contract.ContractResponse;
import com.crm.mcsv_rrhh.dto.contract.CreateContractRequest;
import com.crm.mcsv_rrhh.dto.contract.UpdateContractRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ContractService {
    ContractDetailResponse createContract(CreateContractRequest request, List<MultipartFile> files);

    Page<ContractResponse> list(String search,
                                Long employeeId, Long statusId,
                                Long contractStatusId, Long contractTypeId,
                                Integer costCenter,
                                LocalDate createdFrom, LocalDate createdTo,
                                LocalDate startDateFrom, LocalDate startDateTo,
                                LocalDate endDateFrom, LocalDate endDateTo,
                                LocalDate updatedFrom, LocalDate updatedTo,
                                Pageable pageable, String sortBy, String sortDir);

    Map<String, Long> getStats(Long employeeId);

    ContractDetailResponse getById(Long id);

    ContractDetailResponse updateContract(Long id, UpdateContractRequest req, List<MultipartFile> files);

    byte[] exportCsv();

    BulkImportResult importFromCsv(MultipartFile file);

    List<AttendanceEmployeeSelectItem> getEmployeesForAttendance();

    List<EmployeeSelectItem> getSupervisors();

    List<EmployeeSelectItem> getVisitors();

    List<EmployeeSelectItem> getCompanyRepresentatives();

    record AttendanceEmployeeSelectItem(Long id, String name, Integer costCenter) {}

    record EmployeeSelectItem(Long id, String name) {}
}
