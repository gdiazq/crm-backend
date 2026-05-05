package com.crm.mcsv_project.client;

import com.crm.common.dto.PagedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mcsv-rrhh", fallback = com.crm.mcsv_project.client.fallback.RrhhClientFallback.class)
public interface RrhhClient {

    @GetMapping("/employee/select/supervisors")
    List<PersonSelectItem> getSupervisors();

    @GetMapping("/employee/select/visitors")
    List<PersonSelectItem> getVisitors();

    @GetMapping("/employee/select/company-representatives")
    List<PersonSelectItem> getCompanyRepresentatives();

    @GetMapping("/select/legal-termination-causes")
    List<PersonSelectItem> getLegalTerminationCauses();

    @GetMapping("/select/quality-of-work")
    List<PersonSelectItem> getQualityOfWork();

    @GetMapping("/select/safety-compliances")
    List<PersonSelectItem> getSafetyCompliances();

    @GetMapping("/select/no-re-hired-causes")
    List<PersonSelectItem> getNoReHiredCauses();

    @GetMapping("/employee/paged")
    PagedResponse<EmployeeResponseDTO> getEmployeesByCostCenter(@RequestParam("costCenter") Integer costCenter,
                                                              @RequestParam(value = "search", required = false) String search,
                                                              @RequestParam(value = "active", required = false) Boolean active,
                                                              @RequestParam(value = "statusId", required = false) Long statusId,
                                                              @RequestParam("page") int page,
                                                              @RequestParam("size") int size,
                                                              @RequestParam("sortBy") String sortBy,
                                                              @RequestParam("sortDir") String sortDir);
}
