package com.crm.mcsv_rrhh.controller.educationlevel;

import com.crm.mcsv_rrhh.dto.educationlevel.EducationLevelResponse;
import com.crm.mcsv_rrhh.service.educationlevel.EducationLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/education-levels")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class EducationLevelController {

    private final EducationLevelService service;

    @GetMapping
    @Operation(summary = "Niveles educacionales")
    public ResponseEntity<List<EducationLevelResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
