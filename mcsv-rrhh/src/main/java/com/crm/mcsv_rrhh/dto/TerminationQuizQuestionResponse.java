package com.crm.mcsv_rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminationQuizQuestionResponse {

    private Long id;
    private Long employeeId;
    private String question;
    private String questionGroup;
    private Boolean required;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
