package com.crm.mcsv_recruitment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobOpeningResponse {

    private Long id;
    private String title;

    private Long jobTitleId;
    private String jobTitleName;

    private Integer costCenter;
    private String projectName;

    private Integer headcount;

    private Long supervisorUserId;
    // TODO: resolver vía UserClient cuando se cree (story aparte)
    private String supervisorName;

    private BigDecimal referenceSalary;
    private LocalDate closeDate;
    private String description;
    private String requirements;

    private Long statusId;
    private String statusName;

    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
