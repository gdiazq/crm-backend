package com.crm.mcsv_rrhh.repository.educationlevel;

import com.crm.mcsv_rrhh.entity.educationlevel.EducationLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EducationLevelRepository extends JpaRepository<EducationLevel, Long> {
    Optional<EducationLevel> findByName(String name);
}
