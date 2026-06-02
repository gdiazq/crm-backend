package com.crm.mcsv_rrhh.controller.paymentmethod;

import com.crm.mcsv_rrhh.dto.paymentmethod.PaymentMethodResponse;
import com.crm.mcsv_rrhh.service.paymentmethod.PaymentMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/payment-methods")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class PaymentMethodController {

    private final PaymentMethodService service;

    @GetMapping
    @Operation(summary = "Métodos de pago")
    public ResponseEntity<List<PaymentMethodResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
