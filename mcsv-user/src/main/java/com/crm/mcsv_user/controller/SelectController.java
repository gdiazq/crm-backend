package com.crm.mcsv_user.controller;

import com.crm.mcsv_user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints for dropdown selectors")
public class SelectController {

    private final RoleService roleService;

    @GetMapping("/roles")
    @Operation(summary = "Get roles for selector", description = "Retrieve id and name of all roles")
    public ResponseEntity<List<RoleSelectItem>> getRoles() {
        List<RoleSelectItem> roles = roleService.getAllRoles().stream()
                .map(r -> new RoleSelectItem(r.getId(), r.getName()))
                .toList();
        return ResponseEntity.ok(roles);
    }

    record RoleSelectItem(Long id, String name) {}
}
