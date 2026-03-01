package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.BankRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/banks")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class BankController {

    private final BankRepository bankRepository;

    @GetMapping
    @Operation(summary = "Bancos")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = bankRepository.findAll().stream()
                .map(b -> new Item(b.getId(), b.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
