package com.crm.mcsv_rrhh.client.fallback;

import com.crm.mcsv_rrhh.client.ProjectClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProjectClientFallback implements ProjectClient {

    @Override
    public ProjectNameDTO getByCostCenter(Integer costCenter) {
        log.warn("mcsv-project no disponible — getByCostCenter costCenter={}", costCenter);
        return null;
    }
}
