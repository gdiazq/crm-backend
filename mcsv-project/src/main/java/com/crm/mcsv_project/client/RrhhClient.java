package com.crm.mcsv_project.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "mcsv-rrhh")
public interface RrhhClient {

    @GetMapping("/employee/select/supervisors")
    List<PersonSelectItem> getSupervisors();

    @GetMapping("/employee/select/company-representatives")
    List<PersonSelectItem> getCompanyRepresentatives();
}
