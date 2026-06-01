package com.crm.mcsv_rrhh.controller.bank;

import com.crm.mcsv_rrhh.dto.bank.BankResponse;
import com.crm.mcsv_rrhh.service.bank.BankService;
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

    private final BankService service;

    @GetMapping
    @Operation(summary = "Bancos")
    public ResponseEntity<List<BankResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
