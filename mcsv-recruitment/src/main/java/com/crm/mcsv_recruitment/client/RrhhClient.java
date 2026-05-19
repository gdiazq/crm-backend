package com.crm.mcsv_recruitment.client;

import com.crm.mcsv_recruitment.dto.CatalogItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mcsv-rrhh")
public interface RrhhClient {

    @GetMapping("/select/job-titles/{id}")
    CatalogItem getJobTitleById(@PathVariable Long id);

    @GetMapping("/select/contract-types/{id}")
    CatalogItem getContractTypeById(@PathVariable Long id);
}
