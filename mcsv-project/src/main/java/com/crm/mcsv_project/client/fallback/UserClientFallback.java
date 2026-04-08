package com.crm.mcsv_project.client.fallback;

import com.crm.mcsv_project.client.PersonSelectItem;
import com.crm.mcsv_project.client.UserClient;
import com.crm.mcsv_project.client.UserDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserDetailDTO getUserById(Long id) {
        log.error("mcsv-user no disponible — getUserById fallido para id={}", id);
        throw new RuntimeException("Servicio de usuarios no disponible. Intente más tarde.");
    }

    @Override
    public List<PersonSelectItem> getVisitors() {
        log.warn("mcsv-user no disponible — retornando lista vacía para getVisitors");
        return Collections.emptyList();
    }

    @Override
    public List<PersonSelectItem> getCompanyRepresentatives() {
        log.warn("mcsv-user no disponible — retornando lista vacía para getCompanyRepresentatives");
        return Collections.emptyList();
    }
}
