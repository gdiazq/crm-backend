package com.crm.mcsv_rrhh.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionGroupResponse {
    private Long id;
    private String name;
}
