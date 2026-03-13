package com.crm.mcsv_project.repository;

import com.crm.mcsv_project.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, Long> {

    Optional<ProjectStatus> findByName(String name);

    boolean existsByName(String name);

    List<ProjectStatus> findByActiveTrue();

    @Query("SELECT p FROM ProjectStatus p WHERE " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%',:search,'%'))) AND " +
           "(:active IS NULL OR p.active = :active)")
    Page<ProjectStatus> findAllWithFilters(Pageable pageable,
                                           @Param("search") String search,
                                           @Param("active") Boolean active);
}
