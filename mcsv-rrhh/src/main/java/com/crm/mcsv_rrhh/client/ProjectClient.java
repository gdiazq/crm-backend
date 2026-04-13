package com.crm.mcsv_rrhh.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mcsv-project", fallback = com.crm.mcsv_rrhh.client.fallback.ProjectClientFallback.class)
public interface ProjectClient {

    @GetMapping("/select/cost-centers/{costCenter}")
    ProjectNameDTO getByCostCenter(@PathVariable Integer costCenter);

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class ProjectNameDTO {
        private Integer id;
        private String name;
    }
}
