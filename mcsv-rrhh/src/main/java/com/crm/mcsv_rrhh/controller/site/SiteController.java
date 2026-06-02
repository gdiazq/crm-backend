package com.crm.mcsv_rrhh.controller.site;

import com.crm.mcsv_rrhh.dto.site.SiteResponse;
import com.crm.mcsv_rrhh.service.site.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/sites")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class SiteController {

    private final SiteService service;

    @GetMapping
    @Operation(summary = "Sedes físicas")
    public ResponseEntity<List<SiteResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
