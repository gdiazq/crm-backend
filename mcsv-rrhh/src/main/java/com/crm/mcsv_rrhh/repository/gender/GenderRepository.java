package com.crm.mcsv_rrhh.repository.gender;

import com.crm.mcsv_rrhh.entity.gender.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenderRepository extends JpaRepository<Gender, Long> {
    Optional<Gender> findByName(String name);
}
