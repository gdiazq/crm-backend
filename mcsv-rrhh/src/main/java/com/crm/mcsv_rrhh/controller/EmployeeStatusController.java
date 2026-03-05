package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.EmployeeStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/select/employee-statuses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class EmployeeStatusController {

    private final EmployeeStatusRepository employeeStatusRepository;

    private static final Set<String> APPROVAL_STATUSES = Set.of(
            "Pendiente de revisión", "Pendiente de aprobación", "Aprobado", "Rechazado"
    );

    @GetMapping
    @Operation(summary = "Estados del empleado")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = employeeStatusRepository.findAll().stream()
                .map(e -> new Item(e.getId(), e.getName())).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/approval")
    @Operation(summary = "Estados de aprobación de solicitudes RRHH")
    public ResponseEntity<List<Item>> getApprovalStatuses() {
        List<Item> result = employeeStatusRepository.findAll().stream()
                .filter(e -> APPROVAL_STATUSES.contains(e.getName()))
                .map(e -> new Item(e.getId(), e.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
