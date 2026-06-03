package com.crm.mcsv_rrhh.controller.settlement;

import com.crm.mcsv_rrhh.dto.settlement.QuizQuestionGroupResponse;
import com.crm.mcsv_rrhh.service.settlement.QuizQuestionGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/quiz-question-groups")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class QuizQuestionGroupSelectController {

    private final QuizQuestionGroupService service;

    @GetMapping
    @Operation(summary = "Grupos de preguntas del cuestionario de finiquito")
    public ResponseEntity<List<QuizQuestionGroupResponse>> getAll() {
        return ResponseEntity.ok(service.selectActive());
    }
}
