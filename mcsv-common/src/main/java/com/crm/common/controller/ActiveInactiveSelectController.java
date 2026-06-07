package com.crm.common.controller;

import com.crm.common.dto.SelectItem;
import com.crm.common.service.ActiveInactiveSelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/active-inactive")
@RequiredArgsConstructor
public class ActiveInactiveSelectController {

    private final ActiveInactiveSelectService service;

    @GetMapping
    public ResponseEntity<List<SelectItem>> getAll() {
        return ResponseEntity.ok(service.getOptions());
    }
}
