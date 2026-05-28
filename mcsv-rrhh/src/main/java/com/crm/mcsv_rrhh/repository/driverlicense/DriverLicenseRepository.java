package com.crm.mcsv_rrhh.repository.driverlicense;

import com.crm.mcsv_rrhh.entity.driverlicense.DriverLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverLicenseRepository extends JpaRepository<DriverLicense, Long> {
    Optional<DriverLicense> findByName(String name);
}
