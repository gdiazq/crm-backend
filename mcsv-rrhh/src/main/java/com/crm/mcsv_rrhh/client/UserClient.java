package com.crm.mcsv_rrhh.client;

import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mcsv-user")
public interface UserClient {

    @GetMapping("/detail/{id}")
    UserDTO getUserById(@PathVariable Long id);

    @GetMapping("/select/users/available")
    PagedResponse<UserDTO> getAvailableForEmployee(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> excludeIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);
}
