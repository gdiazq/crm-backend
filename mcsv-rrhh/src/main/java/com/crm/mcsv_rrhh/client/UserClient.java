package com.crm.mcsv_rrhh.client;

import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mcsv-user", fallback = com.crm.mcsv_rrhh.client.fallback.UserClientFallback.class)
public interface UserClient {

    @GetMapping("/detail/{id}")
    UserDTO getUserById(@PathVariable Long id);

    @GetMapping("/detail/batch")
    List<UserDTO> getUsersByIds(@RequestParam List<Long> ids);

    @GetMapping("/select/users/available")
    List<CatalogItem> getAvailableForEmployee(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> excludeIds);

    @GetMapping("/select/users/supervisors")
    List<UserDTO> getSupervisors();

    @GetMapping("/select/users/visitors")
    List<UserDTO> getVisitors();

    @GetMapping("/select/users/company-representatives")
    List<UserDTO> getCompanyRepresentatives();
}
