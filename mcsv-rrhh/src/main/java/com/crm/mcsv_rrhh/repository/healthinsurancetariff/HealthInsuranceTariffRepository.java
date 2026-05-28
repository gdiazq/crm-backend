package com.crm.mcsv_rrhh.repository.healthinsurancetariff;

import com.crm.mcsv_rrhh.entity.healthinsurancetariff.HealthInsuranceTariff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HealthInsuranceTariffRepository extends JpaRepository<HealthInsuranceTariff, Long> {
    Optional<HealthInsuranceTariff> findByName(String name);
}
