package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Profession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfessionRepository extends JpaRepository<Profession, Long> {
    Optional<Profession> findByName(String name);
}
