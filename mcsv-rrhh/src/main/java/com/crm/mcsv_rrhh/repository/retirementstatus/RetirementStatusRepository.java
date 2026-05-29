package com.crm.mcsv_rrhh.repository.retirementstatus;

import com.crm.mcsv_rrhh.entity.retirementstatus.RetirementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RetirementStatusRepository extends JpaRepository<RetirementStatus, Long> {
    Optional<RetirementStatus> findByName(String name);
}
