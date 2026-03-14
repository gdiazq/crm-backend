package com.crm.mcsv_project.repository;

import com.crm.mcsv_project.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, Long>, JpaSpecificationExecutor<ProjectStatus> {

    Optional<ProjectStatus> findByName(String name);

    boolean existsByName(String name);

    List<ProjectStatus> findByActiveTrue();
}
