package com.crm.mcsv_user.service;

import com.crm.mcsv_user.dto.CreateRoleRequest;
import com.crm.mcsv_user.dto.RoleDTO;
import com.crm.mcsv_user.dto.UpdateRoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {

    List<RoleDTO> getAllRoles();

    Page<RoleDTO> getAllRolesPaged(String search, Boolean status, Pageable pageable);

    RoleDTO getRoleById(Long id);

    RoleDTO getRoleByName(String name);

    RoleDTO createRole(CreateRoleRequest request);

    RoleDTO updateRole(Long id, UpdateRoleRequest request);

    void deleteRole(Long id);

    void updateStatus(Long id, Boolean enabled);
}
