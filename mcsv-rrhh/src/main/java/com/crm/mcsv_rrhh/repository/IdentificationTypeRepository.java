package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.IdentificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdentificationTypeRepository extends JpaRepository<IdentificationType, Long> {
    List<IdentificationType> findByStatusTrue();
    Optional<IdentificationType> findByName(String name);
}
