package com.crm.mcsv_user.controller;

import com.crm.mcsv_user.dto.CreateRoleRequest;
import com.crm.mcsv_user.dto.PagedResponse;
import com.crm.mcsv_user.dto.PermissionDTO;
import com.crm.mcsv_user.dto.RoleDTO;
import com.crm.mcsv_user.dto.UpdateRoleRequest;
import com.crm.mcsv_user.service.RoleService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Endpoints for managing roles")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/paged")
    @Operation(summary = "Get all roles (paged)", description = "Retrieve a paginated list of roles with optional search and status filter")
    public ResponseEntity<PagedResponse<RoleDTO>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RoleDTO> result = roleService.getAllRolesPaged(search, status, pageable);
        Map<String, Long> stats = roleService.getRoleStats();
        return ResponseEntity.ok(PagedResponse.of(result, stats.get("total"), stats.get("active")));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Retrieve a role by its ID")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get role by name", description = "Retrieve a role by its name")
    public ResponseEntity<RoleDTO> getRoleByName(@PathVariable String name) {
        RoleDTO role = roleService.getRoleByName(name);
        return ResponseEntity.ok(role);
    }

    @PostMapping("/create")
    @Operation(summary = "Create role", description = "Create a new role")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleDTO createdRole = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @PutMapping("/update")
    @Operation(summary = "Update role", description = "Update an existing role")
    public ResponseEntity<RoleDTO> updateRole(@Valid @RequestBody UpdateRoleRequest request) {
        RoleDTO updatedRole = roleService.updateRole(request.getId(), request);
        return ResponseEntity.ok(updatedRole);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Toggle role status", description = "Enable or disable a role by its ID")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        roleService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", description = "Delete a role by its ID")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get all permissions", description = "Retrieve list of all available permissions")
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        return ResponseEntity.ok(roleService.getAllPermissions());
    }

    @GetMapping("/permissions/select")
    @Operation(summary = "Get permissions for select", description = "Retrieve id and name only, for use in multiselect components")
    public ResponseEntity<List<Map<String, Object>>> getPermissionsForSelect() {
        List<Map<String, Object>> result = roleService.getAllPermissions().stream()
                .map(p -> Map.<String, Object>of("id", p.getId(), "name", p.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "Set permissions", description = "Replace all permissions of a role")
    public ResponseEntity<RoleDTO> setPermissions(@PathVariable Long id, @RequestBody Map<String, Set<Long>> body) {
        return ResponseEntity.ok(roleService.setPermissions(id, body.get("permissionIds")));
    }

    @PostMapping("/{id}/permissions/add")
    @Operation(summary = "Add permissions", description = "Add permissions to a role")
    public ResponseEntity<RoleDTO> addPermissions(@PathVariable Long id, @RequestBody Map<String, Set<Long>> body) {
        return ResponseEntity.ok(roleService.addPermissions(id, body.get("permissionIds")));
    }

    @DeleteMapping("/{id}/permissions")
    @Operation(summary = "Remove permissions", description = "Remove permissions from a role")
    public ResponseEntity<RoleDTO> removePermissions(@PathVariable Long id, @RequestBody Map<String, Set<Long>> body) {
        return ResponseEntity.ok(roleService.removePermissions(id, body.get("permissionIds")));
    }
}
