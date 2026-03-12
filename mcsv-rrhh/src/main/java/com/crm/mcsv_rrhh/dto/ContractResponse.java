package com.crm.mcsv_rrhh.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ContractResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeIdentification;
    private String name;
    private String contractNumber;
    private String contractType;
    private String contractStatus;
    private String company;
    private String jobTitle;
    private String baseSalary;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
}
