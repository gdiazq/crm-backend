package com.crm.mcsv_recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateJobOpeningRequest {

    @NotNull
    @Schema(description = "ID de la vacante a actualizar")
    private Long id;

    @Size(max = 200)
    private String title;

    private Long jobTitleId;

    private Integer costCenter;

    @Min(1)
    private Integer headcount;

    private Long supervisorUserId;

    @DecimalMin("0.0")
    private BigDecimal referenceSalary;

    private LocalDate closeDate;

    private String description;

    private String requirements;
}
