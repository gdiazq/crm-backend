package com.crm.mcsv_project.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponseDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeIdentification;
    private String name;
    private String contractNumber;
    private String contractType;
    private String contractStatus;
    private String approvalStatus;
    private String company;
    private String jobTitle;
    private Integer costCenter;
    private String projectName;
    private String baseSalary;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
