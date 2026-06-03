package com.crm.mcsv_rrhh.service.settlement;

import com.crm.mcsv_rrhh.dto.settlement.QuizQuestionGroupResponse;

import java.util.List;

public interface QuizQuestionGroupService {

    List<QuizQuestionGroupResponse> selectActive();
}
