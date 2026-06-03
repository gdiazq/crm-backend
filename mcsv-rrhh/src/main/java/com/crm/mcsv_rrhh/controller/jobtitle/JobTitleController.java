package com.crm.mcsv_rrhh.controller.jobtitle;

import com.crm.mcsv_rrhh.dto.jobtitle.JobTitleResponse;
import com.crm.mcsv_rrhh.service.jobtitle.JobTitleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/job-titles")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class JobTitleController {

    private final JobTitleService service;

    @GetMapping
    @Operation(summary = "Cargos / Títulos de puesto")
    public ResponseEntity<List<JobTitleResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Cargo por ID")
    public ResponseEntity<JobTitleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
