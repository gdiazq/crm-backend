package com.crm.mcsv_rrhh.controller.safetygroup;

import com.crm.mcsv_rrhh.dto.safetygroup.SafetyGroupResponse;
import com.crm.mcsv_rrhh.service.safetygroup.SafetyGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/safety-groups")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class SafetyGroupController {

    private final SafetyGroupService service;

    @GetMapping
    @Operation(summary = "Agrupaciones de seguridad")
    public ResponseEntity<List<SafetyGroupResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
