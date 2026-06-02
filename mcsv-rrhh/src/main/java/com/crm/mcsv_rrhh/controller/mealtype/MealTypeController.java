package com.crm.mcsv_rrhh.controller.mealtype;

import com.crm.mcsv_rrhh.dto.mealtype.MealTypeResponse;
import com.crm.mcsv_rrhh.service.mealtype.MealTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/meal-types")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class MealTypeController {

    private final MealTypeService service;

    @GetMapping
    @Operation(summary = "Tipos de colación")
    public ResponseEntity<List<MealTypeResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
