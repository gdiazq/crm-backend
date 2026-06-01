package com.crm.mcsv_rrhh.dto.educationlevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationLevelResponse {
    private Long id;
    private String name;
}
