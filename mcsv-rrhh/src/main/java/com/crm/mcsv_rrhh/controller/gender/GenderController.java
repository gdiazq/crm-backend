package com.crm.mcsv_rrhh.controller.gender;

import com.crm.mcsv_rrhh.dto.gender.GenderResponse;
import com.crm.mcsv_rrhh.service.gender.GenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/genders")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class GenderController {

    private final GenderService service;

    @GetMapping
    @Operation(summary = "Géneros")
    public ResponseEntity<List<GenderResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
