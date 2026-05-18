package com.crm.mcsv_recruitment.client.fallback;

import com.crm.mcsv_recruitment.client.RrhhClient;
import com.crm.mcsv_recruitment.dto.CatalogItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RrhhClientFallback implements RrhhClient {

    @Override
    public CatalogItem getJobTitleById(Long id) {
        log.warn("mcsv-rrhh no disponible — getJobTitleById id={}", id);
        return null;
    }

    @Override
    public CatalogItem getContractTypeById(Long id) {
        log.warn("mcsv-rrhh no disponible — getContractTypeById id={}", id);
        return null;
    }
}
