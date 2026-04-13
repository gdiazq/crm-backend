package com.crm.mcsv_rrhh.client.fallback;

import com.crm.mcsv_rrhh.client.UserClient;
import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserDTO getUserById(Long id) {
        log.error("mcsv-user no disponible — getUserById fallido para id={}", id);
        throw new RuntimeException("Servicio de usuarios no disponible. Intente más tarde.");
    }

    @Override
    public List<UserDTO> getUsersByIds(List<Long> ids) {
        log.warn("mcsv-user no disponible — retornando lista vacía para getUsersByIds");
        return Collections.emptyList();
    }

    @Override
    public List<CatalogItem> getAvailableForEmployee(String search, List<Long> excludeIds) {
        log.warn("mcsv-user no disponible — retornando lista vacía para getAvailableForEmployee");
        return Collections.emptyList();
    }

    @Override
    public List<UserDTO> getSupervisors() {
        log.warn("mcsv-user no disponible — retornando lista vacía para getSupervisors");
        return Collections.emptyList();
    }

    @Override
    public List<UserDTO> getVisitors() {
        log.warn("mcsv-user no disponible — retornando lista vacía para getVisitors");
        return Collections.emptyList();
    }

    @Override
    public List<UserDTO> getCompanyRepresentatives() {
        log.warn("mcsv-user no disponible — retornando lista vacía para getCompanyRepresentatives");
        return Collections.emptyList();
    }
}
