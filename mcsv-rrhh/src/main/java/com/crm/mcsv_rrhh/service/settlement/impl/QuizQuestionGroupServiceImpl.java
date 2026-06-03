package com.crm.mcsv_rrhh.service.settlement.impl;

import com.crm.mcsv_rrhh.dto.settlement.QuizQuestionGroupResponse;
import com.crm.mcsv_rrhh.entity.settlement.QuizQuestionGroup;
import com.crm.mcsv_rrhh.repository.settlement.QuizQuestionGroupRepository;
import com.crm.mcsv_rrhh.service.settlement.QuizQuestionGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizQuestionGroupServiceImpl implements QuizQuestionGroupService {

    private final QuizQuestionGroupRepository repository;

    @Override
    public List<QuizQuestionGroupResponse> selectActive() {
        return repository.findByActiveTrueOrderByNameAsc().stream()
                .map(e -> QuizQuestionGroupResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
