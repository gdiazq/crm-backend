package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.PensionStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/pension-statuses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class PensionStatusController {

    private final PensionStatusRepository pensionStatusRepository;

    @GetMapping
    @Operation(summary = "Sistemas de pensión")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = pensionStatusRepository.findAll().stream()
                .map(p -> new Item(p.getId(), p.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
