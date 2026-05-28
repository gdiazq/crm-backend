package com.crm.mcsv_rrhh.repository.emergencycontactrelationship;

import com.crm.mcsv_rrhh.entity.emergencycontactrelationship.EmergencyContactRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmergencyContactRelationshipRepository extends JpaRepository<EmergencyContactRelationship, Long> {
    Optional<EmergencyContactRelationship> findByName(String name);
}
