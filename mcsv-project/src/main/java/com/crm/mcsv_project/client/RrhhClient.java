package com.crm.mcsv_project.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

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
}
