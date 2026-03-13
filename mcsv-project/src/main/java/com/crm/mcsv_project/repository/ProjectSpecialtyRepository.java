package com.crm.mcsv_project.repository;

import com.crm.mcsv_project.entity.ProjectSpecialty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectSpecialtyRepository extends JpaRepository<ProjectSpecialty, Long> {

    Optional<ProjectSpecialty> findByName(String name);

    boolean existsByName(String name);

    List<ProjectSpecialty> findByActiveTrue();

    @Query("SELECT p FROM ProjectSpecialty p WHERE " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%',:search,'%'))) AND " +
           "(:active IS NULL OR p.active = :active)")
    Page<ProjectSpecialty> findAllWithFilters(Pageable pageable,
                                              @Param("search") String search,
                                              @Param("active") Boolean active);
}
