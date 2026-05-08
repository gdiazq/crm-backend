package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.CalendarEventsResponse;
import com.crm.mcsv_rrhh.service.CalendarEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "Eventos agregados para calendario global")
public class CalendarEventController {

    private final CalendarEventService service;

    @GetMapping("/events")
    @Operation(summary = "Listar eventos agregados de calendario")
    public ResponseEntity<CalendarEventsResponse> events(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer costCenter,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(service.findEvents(from, to, module, employeeId, costCenter, status));
    }
}
