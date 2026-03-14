package com.crm.mcsv_user.controller;

import com.crm.mcsv_user.dto.PagedResponse;
import com.crm.mcsv_user.dto.UserResponse;
import com.crm.mcsv_user.repository.PermissionRepository;
import com.crm.mcsv_user.repository.UserRepository;
import com.crm.mcsv_user.service.RoleService;
import com.crm.mcsv_user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints for dropdown selectors")
public class SelectController {

    private final RoleService roleService;
    private final UserService userService;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @GetMapping("/roles")
    @Operation(summary = "Get roles for selector", description = "Retrieve id and name of all roles")
    public ResponseEntity<List<RoleSelectItem>> getRoles() {
        List<RoleSelectItem> roles = roleService.getAllRoles().stream()
                .map(r -> new RoleSelectItem(r.getId(), r.getName()))
                .toList();
        return ResponseEntity.ok(roles);
    }

    record RoleSelectItem(Long id, String name) {}

    @GetMapping("/users/name")
    @Operation(summary = "Get users name for selector", description = "Retrieve id and name of all users")
    public ResponseEntity<List<UserNameSelectItem>> getUserNames() {
        List<UserNameSelectItem> users = userService.getAllUsersForSelect().stream()
                .map(u -> new UserNameSelectItem(
                        u.getId(),
                        (u.getFirstName() != null ? u.getFirstName() : "") + " " + (u.getLastName() != null ? u.getLastName() : "").trim()
                ))
                .toList();
        return ResponseEntity.ok(users);
    }

    record UserNameSelectItem(Long id, String name) {}

    @GetMapping("/users/email")
    @Operation(summary = "Get users email for selector", description = "Retrieve id and email of all users")
    public ResponseEntity<List<UserEmailSelectItem>> getUserEmails() {
        List<UserEmailSelectItem> users = userService.getAllUsersForSelect().stream()
                .map(u -> new UserEmailSelectItem(u.getId(), u.getEmail()))
                .toList();
        return ResponseEntity.ok(users);
    }

    record UserEmailSelectItem(Long id, String email) {}

    @GetMapping("/users/available")
    @Operation(summary = "Get available users for employee assignment", description = "Returns users excluding already linked ones")
    public ResponseEntity<List<Item>> getAvailableForEmployee(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> excludeIds) {
        List<Item> result = userService.getAvailableUsersForEmployee(search, excludeIds).stream()
                .map(u -> new Item(u.getId(), (u.getFirstName() != null ? u.getFirstName() : "") + " " + (u.getLastName() != null ? u.getLastName() : "").trim()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}

    @GetMapping("/status")
    @Operation(summary = "Get status options for selector", description = "Retrieve available status options")
    public ResponseEntity<List<StatusSelectItem>> getStatus() {
        return ResponseEntity.ok(List.of(
                new StatusSelectItem(true, "Activo"),
                new StatusSelectItem(false, "Inactivo")
        ));
    }

    record StatusSelectItem(Boolean id, String name) {}

    @GetMapping("/permissions")
    @Operation(summary = "Get permissions for selector", description = "Retrieve id and name of all permissions")
    public ResponseEntity<List<PermissionSelectItem>> getPermissions() {
        List<PermissionSelectItem> permissions = permissionRepository.findAll().stream()
                .map(p -> new PermissionSelectItem(p.getId(), p.getName()))
                .toList();
        return ResponseEntity.ok(permissions);
    }

    record PermissionSelectItem(Long id, String name) {}

    @GetMapping("/users/supervisors")
    @Operation(summary = "Usuarios supervisores")
    public ResponseEntity<List<UserNameSelectItem>> getSupervisors() {
        List<UserNameSelectItem> users = userRepository.findByRolesNameContainingIgnoreCase("supervisor").stream()
                .map(u -> new UserNameSelectItem(u.getId(),
                        ((u.getFirstName() != null ? u.getFirstName() : "") + " " +
                         (u.getLastName()  != null ? u.getLastName()  : "")).trim()))
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/visitors")
    @Operation(summary = "Usuarios visitadores")
    public ResponseEntity<List<UserNameSelectItem>> getVisitors() {
        List<UserNameSelectItem> users = userRepository.findByRolesNameContainingIgnoreCase("visitador").stream()
                .map(u -> new UserNameSelectItem(u.getId(),
                        ((u.getFirstName() != null ? u.getFirstName() : "") + " " +
                         (u.getLastName()  != null ? u.getLastName()  : "")).trim()))
                .toList();
        return ResponseEntity.ok(users);
    }
}
