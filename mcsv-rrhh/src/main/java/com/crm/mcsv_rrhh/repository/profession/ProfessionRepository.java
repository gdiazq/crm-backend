package com.crm.mcsv_rrhh.repository.profession;

import com.crm.mcsv_rrhh.entity.profession.Profession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfessionRepository extends JpaRepository<Profession, Long> {
    Optional<Profession> findByName(String name);
}
