package com.crm.mcsv_rrhh.repository.healthinsurance;

import com.crm.mcsv_rrhh.entity.healthinsurance.HealthInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HealthInsuranceRepository extends JpaRepository<HealthInsurance, Long> {
    Optional<HealthInsurance> findByName(String name);
}
