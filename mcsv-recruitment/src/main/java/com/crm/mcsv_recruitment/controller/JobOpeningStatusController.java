package com.crm.mcsv_recruitment.controller;

import com.crm.mcsv_recruitment.repository.JobOpeningStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/job-opening-statuses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class JobOpeningStatusController {

    private final JobOpeningStatusRepository jobOpeningStatusRepository;

    @GetMapping
    @Operation(summary = "Estados de vacante")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = jobOpeningStatusRepository.findAll().stream()
                .map(e -> new Item(e.getId(), e.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
