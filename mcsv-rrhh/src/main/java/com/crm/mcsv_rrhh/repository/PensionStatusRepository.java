package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.PensionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PensionStatusRepository extends JpaRepository<PensionStatus, Long> {
    Optional<PensionStatus> findByName(String name);
}
