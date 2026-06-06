package com.crm.mcsv_user.controller;

import com.crm.mcsv_user.dto.select.PermissionSelectItem;
import com.crm.mcsv_user.dto.select.RoleSelectItem;
import com.crm.mcsv_user.dto.select.StatusSelectItem;
import com.crm.mcsv_user.dto.select.UserEmailSelectItem;
import com.crm.mcsv_user.dto.select.UserSelectItem;
import com.crm.mcsv_user.enums.StatusOption;
import com.crm.mcsv_user.mapper.SelectMapper;
import com.crm.mcsv_user.service.RoleService;
import com.crm.mcsv_user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/select")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints for dropdown selectors")
public class SelectController {

    private final RoleService roleService;
    private final UserService userService;
    private final SelectMapper selectMapper;

    @GetMapping("/roles")
    @Operation(summary = "Get roles for selector", description = "Retrieve id and name of all roles")
    public ResponseEntity<List<RoleSelectItem>> getRoles() {
        return ResponseEntity.ok(roleService.selectRoles());
    }

    @GetMapping("/users/name")
    @Operation(summary = "Get users name for selector", description = "Retrieve id and name of all users")
    public ResponseEntity<List<UserSelectItem>> getUserNames() {
        return ResponseEntity.ok(userService.selectUserNames());
    }

    @GetMapping("/users/email")
    @Operation(summary = "Get users email for selector", description = "Retrieve id and email of all users")
    public ResponseEntity<List<UserEmailSelectItem>> getUserEmails() {
        return ResponseEntity.ok(userService.selectUserEmails());
    }

    @GetMapping("/users/available")
    @Operation(summary = "Get available users for employee assignment", description = "Returns users excluding already linked ones")
    public ResponseEntity<List<UserSelectItem>> getAvailableForEmployee(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> excludeIds) {
        return ResponseEntity.ok(userService.selectAvailableForEmployee(search, excludeIds));
    }

    @GetMapping("/status")
    @Operation(summary = "Get status options for selector", description = "Retrieve available status options")
    public ResponseEntity<List<StatusSelectItem>> getStatus() {
        return ResponseEntity.ok(
                Arrays.stream(StatusOption.values())
                        .map(selectMapper::toStatusSelectItem)
                        .toList());
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get permissions for selector", description = "Retrieve id and name of all permissions")
    public ResponseEntity<List<PermissionSelectItem>> getPermissions() {
        return ResponseEntity.ok(roleService.selectPermissions());
    }

    @GetMapping("/users/supervisors")
    @Operation(summary = "Usuarios supervisores")
    public ResponseEntity<List<UserSelectItem>> getSupervisors() {
        return ResponseEntity.ok(userService.selectUsersByRole("supervisor"));
    }

    @GetMapping("/users/visitors")
    @Operation(summary = "Usuarios visitadores")
    public ResponseEntity<List<UserSelectItem>> getVisitors() {
        return ResponseEntity.ok(userService.selectUsersByRole("visitador"));
    }

    @GetMapping("/users/company-representatives")
    @Operation(summary = "Usuarios representantes de empresa")
    public ResponseEntity<List<UserSelectItem>> getCompanyRepresentatives() {
        return ResponseEntity.ok(userService.selectUsersByRole("representante"));
    }
}
