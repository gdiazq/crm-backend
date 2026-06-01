package com.crm.mcsv_rrhh.controller.driverlicense;

import com.crm.mcsv_rrhh.dto.driverlicense.DriverLicenseResponse;
import com.crm.mcsv_rrhh.service.driverlicense.DriverLicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/driver-licenses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class DriverLicenseController {

    private final DriverLicenseService service;

    @GetMapping
    @Operation(summary = "Tipos de licencia de conducir")
    public ResponseEntity<List<DriverLicenseResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
