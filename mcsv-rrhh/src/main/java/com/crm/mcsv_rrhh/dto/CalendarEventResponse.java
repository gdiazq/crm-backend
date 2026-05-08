package com.crm.mcsv_rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventResponse {
    private String id;
    private String date;
    private String title;
    private String description;
    private String module;
    private Long entityId;
    private String entityType;
    private String status;
    private Long employeeId;
    private String employeeFullName;
    private Integer costCenter;
    private String projectName;
    private String tone;
}
