package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.repository.ProjectRepository;
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
@RequestMapping("/select/cost-centers")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class ProjectCostCenterSelectController {

    private final ProjectRepository projectRepository;

    @GetMapping
    @Operation(summary = "Centros de costo de proyectos activos")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = projectRepository.findByActiveTrue().stream()
                .map(p -> new Item(p.getCostCenter(), p.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{costCenter}")
    @Operation(summary = "Obtener proyecto por centro de costo")
    public ResponseEntity<Item> getByCostCenter(@PathVariable Integer costCenter) {
        return projectRepository.findByCostCenter(costCenter)
                .map(p -> ResponseEntity.ok(new Item(p.getCostCenter(), p.getName())))
                .orElse(ResponseEntity.notFound().build());
    }

    record Item(Integer id, String name) {}
}
