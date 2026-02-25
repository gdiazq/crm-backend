package com.crm.mcsv_rrhh.client;

import com.crm.mcsv_rrhh.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mcsv-user")
public interface UserClient {

    @GetMapping("/detail/{id}")
    UserDTO getUserById(@PathVariable Long id);
}
