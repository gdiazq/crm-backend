package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.DriverLicenseRepository;
import com.crm.mcsv_rrhh.repository.EducationLevelRepository;
import com.crm.mcsv_rrhh.repository.GenderRepository;
import com.crm.mcsv_rrhh.repository.IdentificationTypeRepository;
import com.crm.mcsv_rrhh.repository.MaritalStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class IdentificationTypeController {

    private final IdentificationTypeRepository identificationTypeRepository;
    private final GenderRepository genderRepository;
    private final MaritalStatusRepository maritalStatusRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final DriverLicenseRepository driverLicenseRepository;

    @GetMapping("/identification-types")
    @Operation(summary = "Tipos de identificación activos")
    public ResponseEntity<List<IdentificationTypeItem>> getIdentificationTypes() {
        List<IdentificationTypeItem> result = identificationTypeRepository.findByStatusTrue().stream()
                .map(t -> new IdentificationTypeItem(t.getId(), t.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/genders")
    @Operation(summary = "Géneros")
    public ResponseEntity<List<GenderItem>> getGenders() {
        List<GenderItem> result = genderRepository.findAll().stream()
                .map(g -> new GenderItem(g.getId(), g.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/marital-statuses")
    @Operation(summary = "Estados civiles")
    public ResponseEntity<List<MaritalStatusItem>> getMaritalStatuses() {
        List<MaritalStatusItem> result = maritalStatusRepository.findAll().stream()
                .map(m -> new MaritalStatusItem(m.getId(), m.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/education-levels")
    @Operation(summary = "Niveles educacionales")
    public ResponseEntity<List<EducationLevelItem>> getEducationLevels() {
        List<EducationLevelItem> result = educationLevelRepository.findAll().stream()
                .map(e -> new EducationLevelItem(e.getId(), e.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/driver-licenses")
    @Operation(summary = "Tipos de licencia de conducir")
    public ResponseEntity<List<DriverLicenseItem>> getDriverLicenses() {
        List<DriverLicenseItem> result = driverLicenseRepository.findAll().stream()
                .map(d -> new DriverLicenseItem(d.getId(), d.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record IdentificationTypeItem(Long id, String name) {}
    record GenderItem(Long id, String name) {}
    record MaritalStatusItem(Long id, String name) {}
    record EducationLevelItem(Long id, String name) {}
    record DriverLicenseItem(Long id, String name) {}
}
