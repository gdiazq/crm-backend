package com.crm.mcsv_project.repository;

import com.crm.mcsv_project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    boolean existsByCostCenter(Integer costCenter);

    boolean existsByCostCenterAndIdNot(Integer costCenter, Long id);

    List<Project> findByActiveTrue();

    Optional<Project> findByCostCenter(Integer costCenter);
}
