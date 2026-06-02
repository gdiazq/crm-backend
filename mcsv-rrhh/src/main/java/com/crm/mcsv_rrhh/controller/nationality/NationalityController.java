package com.crm.mcsv_rrhh.controller.nationality;

import com.crm.mcsv_rrhh.dto.nationality.NationalityResponse;
import com.crm.mcsv_rrhh.service.nationality.NationalityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/nationalities")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class NationalityController {

    private final NationalityService service;

    @GetMapping
    @Operation(summary = "Nacionalidades")
    public ResponseEntity<List<NationalityResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
