package com.crm.mcsv_rrhh.dto.safetygroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyGroupResponse {
    private Long id;
    private String name;
}
