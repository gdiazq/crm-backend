package com.crm.mcsv_user.service;

import com.crm.mcsv_user.dto.RoleDTO;

import java.util.List;

public interface RoleService {

    List<RoleDTO> getAllRoles();

    RoleDTO getRoleById(Long id);

    RoleDTO getRoleByName(String name);

    RoleDTO createRole(String name, String description);

    void deleteRole(Long id);
}
