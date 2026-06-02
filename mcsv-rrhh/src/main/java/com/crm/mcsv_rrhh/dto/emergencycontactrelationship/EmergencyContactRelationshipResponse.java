package com.crm.mcsv_rrhh.dto.emergencycontactrelationship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactRelationshipResponse {
    private Long id;
    private String name;
}
