package com.crm.mcsv_rrhh.service.emergencycontactrelationship;

import com.crm.mcsv_rrhh.dto.emergencycontactrelationship.EmergencyContactRelationshipResponse;

import java.util.List;

public interface EmergencyContactRelationshipService {

    List<EmergencyContactRelationshipResponse> selectAll();
}
