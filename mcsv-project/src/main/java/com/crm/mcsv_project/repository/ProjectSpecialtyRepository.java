package com.crm.mcsv_project.repository;

import com.crm.mcsv_project.entity.ProjectSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProjectSpecialtyRepository extends JpaRepository<ProjectSpecialty, Long>, JpaSpecificationExecutor<ProjectSpecialty> {

    Optional<ProjectSpecialty> findByName(String name);

    boolean existsByName(String name);

    List<ProjectSpecialty> findByActiveTrue();
}
