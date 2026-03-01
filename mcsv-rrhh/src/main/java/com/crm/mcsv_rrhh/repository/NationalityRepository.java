package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Nationality;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NationalityRepository extends JpaRepository<Nationality, Long> {
    Optional<Nationality> findByName(String name);
}
