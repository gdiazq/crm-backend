package com.crm.mcsv_project.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {

    private Long id;
    private Long userId;
    private String identification;
    private String firstName;
    private String paternalLastName;
    private String maternalLastName;
    private String corporateEmail;
    private String phone;
    private String statusName;
    private Integer costCenter;
    private String projectName;
    private Boolean active;
    private Boolean rehireEligible;
    private Boolean hasContract;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
