package com.crm.mcsv_project.repository;

import com.crm.mcsv_project.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, Long> {

    Optional<ProjectStatus> findByName(String name);

    boolean existsByName(String name);

    List<ProjectStatus> findByActiveTrue();

    @Query("SELECT p FROM ProjectStatus p WHERE " +
           "('' = :search OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:active IS NULL OR p.active = :active) AND " +
           "(:createdFrom IS NULL OR p.createdAt >= :createdFrom) AND " +
           "(:createdTo IS NULL OR p.createdAt <= :createdTo) AND " +
           "(:updatedFrom IS NULL OR p.updatedAt >= :updatedFrom) AND " +
           "(:updatedTo IS NULL OR p.updatedAt <= :updatedTo)")
    Page<ProjectStatus> findAllWithFilters(Pageable pageable,
                                           @Param("search") String search,
                                           @Param("active") Boolean active,
                                           @Param("createdFrom") LocalDateTime createdFrom,
                                           @Param("createdTo") LocalDateTime createdTo,
                                           @Param("updatedFrom") LocalDateTime updatedFrom,
                                           @Param("updatedTo") LocalDateTime updatedTo);
}
