package com.crm.mcsv_rrhh.controller;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.AttendanceResponse;
import com.crm.mcsv_rrhh.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Gestión de asistencia diaria")
public class AttendanceController {

    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    private static final java.util.Map<String, String> SORT_FIELD_ALIASES = java.util.Map.of(
            "checkInDate", "checkInTime",
            "checkOutDate", "checkOutTime"
    );

    private final AttendanceService service;

    @GetMapping("/paged")
    @Operation(summary = "Listar asistencia diaria (paginado)")
    public ResponseEntity<PagedResponse<AttendanceResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer costCenter,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String effectiveSortBy = SORT_FIELD_ALIASES.getOrDefault(sortBy, sortBy);

        Sort sort = EMPLOYEE_SORT_FIELDS.contains(effectiveSortBy)
                ? Sort.unsorted()
                : (sortDir.equalsIgnoreCase("asc") ? Sort.by(effectiveSortBy).ascending() : Sort.by(effectiveSortBy).descending());
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(service.list(search, employeeId, costCenter, statusId,
                dateFrom, dateTo, createdFrom, createdTo, updatedFrom, updatedTo, pageable, effectiveSortBy, sortDir));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener asistencia por ID")
    public ResponseEntity<AttendanceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/select/by-cost-center/{costCenter}")
    @Operation(summary = "Listar asistencia por centro de costo")
    public ResponseEntity<List<AttendanceResponse>> selectByCostCenter(@PathVariable Integer costCenter) {
        return ResponseEntity.ok(service.findByCostCenter(costCenter));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar asistencia a CSV")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer costCenter,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(service.exportCsv(search, employeeId, costCenter, statusId, dateFrom, dateTo));
    }
}
