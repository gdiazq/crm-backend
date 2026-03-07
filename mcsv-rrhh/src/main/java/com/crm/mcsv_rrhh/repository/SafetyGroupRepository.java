package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.SafetyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SafetyGroupRepository extends JpaRepository<SafetyGroup, Long> {
    Optional<SafetyGroup> findByName(String name);
}
