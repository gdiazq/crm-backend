package com.crm.mcsv_user.service.impl;

import com.crm.common.dto.BulkImportResult;
import com.crm.mcsv_user.dto.CreateRoleRequest;
import com.crm.mcsv_user.dto.PermissionDTO;
import com.crm.mcsv_user.dto.RoleDTO;
import com.crm.mcsv_user.dto.UpdateRoleRequest;
import com.crm.mcsv_user.entity.Permission;
import com.crm.mcsv_user.entity.Role;
import com.crm.mcsv_user.entity.User;
import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_user.mapper.UserMapper;
import com.crm.mcsv_user.repository.PermissionRepository;
import com.crm.mcsv_user.repository.RoleRepository;
import com.crm.mcsv_user.repository.UserRepository;
import com.crm.mcsv_user.service.RoleService;
import com.crm.mcsv_user.service.RoleProvisioningService;
import com.crm.mcsv_user.service.RoleNotificationService;
import com.crm.common.util.CsvUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserMapper userMapper;
    private final RoleProvisioningService roleProvisioningService;
    private final RoleNotificationService roleNotificationService;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        log.info("Fetching all roles");
        return roleRepository.findAll()
                .stream()
                .map(userMapper::roleToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDTO> getAllRolesPaged(String search, Boolean status, Pageable pageable) {
        log.info("Fetching roles paged, search: {}, status: {}", search, status);
        String safeSearch = (search != null && !search.isBlank()) ? search.trim() : "";
        return roleRepository.filterRoles(safeSearch, status, pageable).map(userMapper::roleToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        log.info("Fetching role by id: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return userMapper.roleToDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleByName(String name) {
        log.info("Fetching role by name: {}", name);
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
        return userMapper.roleToDTO(role);
    }

    @Override
    public RoleDTO createRole(CreateRoleRequest request) {
        return roleProvisioningService.createRole(request);
    }

    @Override
    @Transactional
    public RoleDTO updateRole(Long id, UpdateRoleRequest request) {
        log.info("Updating role with id: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (request.getName() != null && !request.getName().equals(role.getName())) {
            if (roleRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Role already exists with name: " + request.getName());
            }
            role.setName(request.getName());
        }

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        Role updatedRole = roleRepository.save(role);
        log.info("Role updated successfully with id: {}", updatedRole.getId());

        roleNotificationService.notifyRoleUpdated(updatedRole.getId(), updatedRole.getName());

        return userMapper.roleToDTO(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        log.info("Deleting role with id: {}", id);

        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }

        roleRepository.deleteById(id);
        log.info("Role deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getRoleStats() {
        long total = roleRepository.count();
        long active = roleRepository.countByEnabled(true);
        return Map.of("total", total, "active", active);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Boolean enabled) {
        log.info("Updating status for role id: {} to {}", id, enabled);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        role.setEnabled(enabled);
        roleRepository.save(role);

        List<User> users = userRepository.findAllByRolesId(id);
        users.forEach(u -> u.setEnabled(enabled));
        userRepository.saveAll(users);

        log.info("Role status updated to {}. Affected {} users.", enabled, users.size());

        roleNotificationService.notifyRoleStatusChanged(id, role.getName(), Boolean.TRUE.equals(enabled));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissions() {
        log.info("Fetching all permissions");
        return permissionRepository.findAll().stream()
                .map(p -> PermissionDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .label(p.getLabel())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleDTO setPermissions(Long roleId, Set<Long> permissionIds) {
        log.info("Setting permissions for role id: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        List<Permission> permissions = permissionRepository.findAllByIdIn(permissionIds);
        role.setPermissions(new HashSet<>(permissions));
        RoleDTO result = userMapper.roleToDTO(roleRepository.save(role));
        roleNotificationService.notifyPermissionsChanged(role.getId(), role.getName(),
                "Los permisos de tu rol \"" + role.getName() + "\" han sido reemplazados por un administrador.");
        return result;
    }

    @Override
    @Transactional
    public RoleDTO addPermissions(Long roleId, Set<Long> permissionIds) {
        log.info("Adding permissions to role id: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        List<Permission> permissions = permissionRepository.findAllByIdIn(permissionIds);
        role.getPermissions().addAll(permissions);
        RoleDTO result = userMapper.roleToDTO(roleRepository.save(role));
        roleNotificationService.notifyPermissionsChanged(role.getId(), role.getName(),
                "Se han agregado permisos a tu rol \"" + role.getName() + "\".");
        return result;
    }

    @Override
    @Transactional
    public RoleDTO removePermissions(Long roleId, Set<Long> permissionIds) {
        log.info("Removing permissions from role id: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        role.getPermissions().removeIf(p -> permissionIds.contains(p.getId()));
        RoleDTO result = userMapper.roleToDTO(roleRepository.save(role));
        roleNotificationService.notifyPermissionsChanged(role.getId(), role.getName(),
                "Se han eliminado permisos de tu rol \"" + role.getName() + "\".");
        return result;
    }

    @Override
    public BulkImportResult importRolesFromCsv(MultipartFile file) {
        return CsvUtil.importNameDesc(file, (name, description) -> {
            CreateRoleRequest request = CreateRoleRequest.builder()
                    .name(name)
                    .description(description)
                    .build();
            roleProvisioningService.createRole(request);
        });
    }

    @Override
    public byte[] exportCsv() {
        return CsvUtil.build(
                "ID,Nombre,Descripción,Habilitado,Permisos",
                roleRepository.findAll(),
                r -> {
                    String permissions = r.getPermissions().stream()
                            .map(Permission::getName).reduce("", (a, b) -> a.isEmpty() ? b : a + "|" + b);
                    return r.getId() + "," +
                            CsvUtil.escape(r.getName()) + "," +
                            CsvUtil.escape(r.getDescription()) + "," +
                            r.getEnabled() + "," +
                            CsvUtil.escape(permissions);
                });
    }
}
