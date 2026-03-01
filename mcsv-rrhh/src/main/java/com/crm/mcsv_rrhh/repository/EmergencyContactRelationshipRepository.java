package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.EmergencyContactRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmergencyContactRelationshipRepository extends JpaRepository<EmergencyContactRelationship, Long> {
    Optional<EmergencyContactRelationship> findByName(String name);
}
