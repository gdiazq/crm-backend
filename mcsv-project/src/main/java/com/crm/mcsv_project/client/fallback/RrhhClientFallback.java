package com.crm.mcsv_project.client.fallback;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_project.client.EmployeeResponseDTO;
import com.crm.mcsv_project.client.PersonSelectItem;
import com.crm.mcsv_project.client.RrhhClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RrhhClientFallback implements RrhhClient {

    @Override
    public List<PersonSelectItem> getSupervisors() {
        log.warn("mcsv-rrhh no disponible — retornando lista vacía para getSupervisors");
        return Collections.emptyList();
    }

    @Override
    public List<PersonSelectItem> getVisitors() {
        log.warn("mcsv-rrhh no disponible — retornando lista vacía para getVisitors");
        return Collections.emptyList();
    }

    @Override
    public List<PersonSelectItem> getCompanyRepresentatives() {
        log.warn("mcsv-rrhh no disponible — retornando lista vacía para getCompanyRepresentatives");
        return Collections.emptyList();
    }

    @Override
    public List<PersonSelectItem> getLegalTerminationCauses() {
        log.warn("mcsv-rrhh no disponible — retornando lista vacía para getLegalTerminationCauses");
        return Collections.emptyList();
    }

    @Override
    public List<PersonSelectItem> getQualityOfWork() {
        log.warn("mcsv-rrhh no disponible — retornando lista vacía para getQualityOfWork");
        return Collections.emptyList();
    }

    @Override
    public List<PersonSelectItem> getSafetyCompliances() {
        log.warn("mcsv-rrhh no disponible — retornando lista vacía para getSafetyCompliances");
        return Collections.emptyList();
    }

    @Override
    public List<PersonSelectItem> getNoReHiredCauses() {
        log.warn("mcsv-rrhh no disponible — retornando lista vacía para getNoReHiredCauses");
        return Collections.emptyList();
    }

    @Override
    public PagedResponse<EmployeeResponseDTO> getEmployeesByCostCenter(Integer costCenter,
                                                                     String search,
                                                                     Boolean active,
                                                                     Long statusId,
                                                                     int page,
                                                                     int size,
                                                                     String sortBy,
                                                                     String sortDir) {
        log.warn("mcsv-rrhh no disponible — retornando página vacía para getEmployeesByCostCenter costCenter={}", costCenter);
        return PagedResponse.<EmployeeResponseDTO>builder()
                .content(Collections.emptyList())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .total(0)
                .active(0)
                .build();
    }
}
