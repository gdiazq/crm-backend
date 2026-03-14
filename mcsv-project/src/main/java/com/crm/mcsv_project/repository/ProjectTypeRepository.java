package com.crm.mcsv_project.repository;

import com.crm.mcsv_project.entity.ProjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProjectTypeRepository extends JpaRepository<ProjectType, Long>, JpaSpecificationExecutor<ProjectType> {

    Optional<ProjectType> findByName(String name);

    boolean existsByName(String name);

    List<ProjectType> findByActiveTrue();
}
