package com.crm.mcsv_project.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "mcsv-user", fallback = com.crm.mcsv_project.client.fallback.UserClientFallback.class)
public interface UserClient {

    @GetMapping("/detail/{id}")
    UserDetailDTO getUserById(@PathVariable Long id);

    @GetMapping("/select/users/visitors")
    List<PersonSelectItem> getVisitors();

    @GetMapping("/select/users/company-representatives")
    List<PersonSelectItem> getCompanyRepresentatives();
}
