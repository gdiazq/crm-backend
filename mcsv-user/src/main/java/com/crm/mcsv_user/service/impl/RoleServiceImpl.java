package com.crm.mcsv_user.service.impl;

import com.crm.mcsv_user.client.NotificationClient;
import com.crm.mcsv_user.dto.BulkImportResult;
import com.crm.mcsv_user.dto.CreateRoleRequest;
import com.crm.mcsv_user.dto.PermissionDTO;
import com.crm.mcsv_user.dto.RoleDTO;
import com.crm.mcsv_user.dto.SendNotificationRequest;
import com.crm.mcsv_user.dto.UpdateRoleRequest;
import com.crm.mcsv_user.entity.Permission;
import com.crm.mcsv_user.entity.Role;
import com.crm.mcsv_user.entity.User;
import com.crm.mcsv_user.exception.DuplicateResourceException;
import com.crm.mcsv_user.exception.ResourceNotFoundException;
import com.crm.mcsv_user.mapper.UserMapper;
import com.crm.mcsv_user.repository.PermissionRepository;
import com.crm.mcsv_user.repository.RoleRepository;
import com.crm.mcsv_user.repository.UserRepository;
import com.crm.mcsv_user.service.RoleService;
import com.crm.mcsv_user.util.CsvUtil;
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
    private final NotificationClient notificationClient;

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
    @Transactional
    public RoleDTO createRole(CreateRoleRequest request) {
        log.info("Creating new role with name: {}", request.getName());

        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role already exists with name: " + request.getName());
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully with id: {}", savedRole.getId());

        roleRepository.findByName("ROLE_ADMIN").ifPresent(adminRole -> {
            List<User> admins = userRepository.findAllByRolesId(adminRole.getId());
            admins.forEach(admin -> {
                try {
                    notificationClient.send(SendNotificationRequest.builder()
                            .userId(admin.getId())
                            .title("Nuevo rol creado")
                            .message("Se ha creado el rol \"" + savedRole.getName() + "\" en el sistema.")
                            .type("INFO")
                            .build());
                } catch (Exception e) {
                    log.warn("Failed to send new role notification to userId: {}", admin.getId(), e);
                }
            });
        });

        return userMapper.roleToDTO(savedRole);
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

        List<User> users = userRepository.findAllByRolesId(updatedRole.getId());
        users.forEach(u -> {
            try {
                notificationClient.send(SendNotificationRequest.builder()
                        .userId(u.getId())
                        .title("Rol actualizado")
                        .message("Tu rol \"" + updatedRole.getName() + "\" ha sido actualizado por un administrador.")
                        .type("INFO")
                        .build());
            } catch (Exception e) {
                log.warn("Failed to send role updated notification to userId: {}", u.getId(), e);
            }
        });

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

        String statusLabel = Boolean.TRUE.equals(enabled) ? "activado" : "desactivado";
        String userTitle   = Boolean.TRUE.equals(enabled) ? "Cuenta reactivada" : "Cuenta desactivada";
        String userMessage = Boolean.TRUE.equals(enabled)
                ? "Tu cuenta ha sido reactivada porque el rol \"" + role.getName() + "\" fue activado."
                : "Tu cuenta ha sido desactivada porque el rol \"" + role.getName() + "\" fue desactivado.";
        String userType = Boolean.TRUE.equals(enabled) ? "SUCCESS" : "WARNING";

        // Notificar a los usuarios afectados
        users.forEach(u -> {
            try {
                notificationClient.send(SendNotificationRequest.builder()
                        .userId(u.getId())
                        .title(userTitle)
                        .message(userMessage)
                        .type(userType)
                        .build());
            } catch (Exception e) {
                log.warn("Failed to send status notification to userId: {}", u.getId(), e);
            }
        });

        // Notificar a los admins
        roleRepository.findByName("ROLE_ADMIN").ifPresent(adminRole -> {
            List<User> admins = userRepository.findAllByRolesId(adminRole.getId());
            admins.forEach(admin -> {
                try {
                    notificationClient.send(SendNotificationRequest.builder()
                            .userId(admin.getId())
                            .title("Rol " + statusLabel)
                            .message("El rol \"" + role.getName() + "\" ha sido " + statusLabel + ". " + users.size() + " usuario(s) afectado(s).")
                            .type("INFO")
                            .build());
                } catch (Exception e) {
                    log.warn("Failed to send role status notification to admin userId: {}", admin.getId(), e);
                }
            });
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissions() {
        log.info("Fetching all permissions");
        return permissionRepository.findAll().stream()
                .map(p -> PermissionDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
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
        notifyPermissionChange(role, "Los permisos de tu rol \"" + role.getName() + "\" han sido reemplazados por un administrador.");
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
        notifyPermissionChange(role, "Se han agregado permisos a tu rol \"" + role.getName() + "\".");
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
        notifyPermissionChange(role, "Se han eliminado permisos de tu rol \"" + role.getName() + "\".");
        return result;
    }

    private void notifyPermissionChange(Role role, String userMessage) {
        List<User> users = userRepository.findAllByRolesId(role.getId());

        users.forEach(u -> {
            try {
                notificationClient.send(SendNotificationRequest.builder()
                        .userId(u.getId())
                        .title("Permisos actualizados")
                        .message(userMessage)
                        .type("INFO")
                        .build());
            } catch (Exception e) {
                log.warn("Failed to send permission change notification to userId: {}", u.getId(), e);
            }
        });

        roleRepository.findByName("ROLE_ADMIN").ifPresent(adminRole -> {
            List<User> admins = userRepository.findAllByRolesId(adminRole.getId());
            admins.forEach(admin -> {
                try {
                    notificationClient.send(SendNotificationRequest.builder()
                            .userId(admin.getId())
                            .title("Permisos de rol actualizados")
                            .message("Los permisos del rol \"" + role.getName() + "\" han sido modificados. " + users.size() + " usuario(s) afectado(s).")
                            .type("INFO")
                            .build());
                } catch (Exception e) {
                    log.warn("Failed to send permission change notification to admin userId: {}", admin.getId(), e);
                }
            });
        });
    }

    @Override
    public BulkImportResult importRolesFromCsv(MultipartFile file) {
        return CsvUtil.importNameDesc(file, (name, description) -> {
            CreateRoleRequest request = CreateRoleRequest.builder()
                    .name(name)
                    .description(description)
                    .build();
            createRole(request);
        });
    }

    @Override
    public byte[] exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Nombre,Descripción,Habilitado,Permisos\n");
        roleRepository.findAll().forEach(r -> {
            String permissions = r.getPermissions().stream()
                    .map(p -> p.getName()).reduce("", (a, b) -> a.isEmpty() ? b : a + "|" + b);
            csv.append(r.getId()).append(",")
               .append(escape(r.getName())).append(",")
               .append(escape(r.getDescription())).append(",")
               .append(r.getEnabled()).append(",")
               .append(escape(permissions)).append("\n");
        });
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }
}
