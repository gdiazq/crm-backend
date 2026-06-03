package com.crm.mcsv_rrhh.controller.employee;

import com.crm.mcsv_rrhh.dto.employee.EmployeeStatusResponse;
import com.crm.mcsv_rrhh.service.employee.EmployeeStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/employee-statuses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class EmployeeStatusController {

    private final EmployeeStatusService service;

    @GetMapping
    @Operation(summary = "Estados del empleado")
    public ResponseEntity<List<EmployeeStatusResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }

    @GetMapping("/approval")
    @Operation(summary = "Estados de aprobación de solicitudes RRHH")
    public ResponseEntity<List<EmployeeStatusResponse>> getApprovalStatuses() {
        return ResponseEntity.ok(service.selectApprovalStatuses());
    }
}
