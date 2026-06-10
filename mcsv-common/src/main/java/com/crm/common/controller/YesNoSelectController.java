package com.crm.common.controller;

import com.crm.common.dto.SelectItem;
import com.crm.common.service.yesnoselect.YesNoSelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/yes-no")
@RequiredArgsConstructor
public class YesNoSelectController {

    private final YesNoSelectService service;

    @GetMapping
    public ResponseEntity<List<SelectItem>> getAll() {
        return ResponseEntity.ok(service.getOptions());
    }
}
