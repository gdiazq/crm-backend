package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.TerminationQuizQuestionResponse;
import com.crm.mcsv_rrhh.service.TerminationQuizQuestionService;
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
@Tag(name = "TerminationQuizQuestion Select", description = "Select de preguntas activas del cuestionario de finiquito")
public class TerminationQuizQuestionSelectController {

    private final TerminationQuizQuestionService service;

    @GetMapping("/termination-quiz-questions")
    @Operation(summary = "Obtener preguntas activas del cuestionario")
    public ResponseEntity<List<TerminationQuizQuestionResponse>> getActiveQuestions() {
        return ResponseEntity.ok(service.getActiveQuestions());
    }

}
