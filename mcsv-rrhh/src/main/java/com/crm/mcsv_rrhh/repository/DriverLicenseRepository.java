package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.DriverLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverLicenseRepository extends JpaRepository<DriverLicense, Long> {
    Optional<DriverLicense> findByName(String name);
}
