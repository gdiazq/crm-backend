package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.repository.ProjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/projects")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class ProjectSelectController {

    private final ProjectRepository projectRepository;

    @GetMapping
    @Operation(summary = "Proyectos activos")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = projectRepository.findByActiveTrue().stream()
                .map(p -> new Item(p.getId(), p.getCostCenter() + " - " + p.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
