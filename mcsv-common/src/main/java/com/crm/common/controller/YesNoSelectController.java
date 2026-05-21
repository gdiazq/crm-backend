package com.crm.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/yes-no")
public class YesNoSelectController {

    @GetMapping
    public ResponseEntity<List<Item>> getAll() {
        return ResponseEntity.ok(List.of(
                new Item(1L, "Sí"),
                new Item(2L, "No")
        ));
    }

    public record Item(Long id, String name) {}
}
