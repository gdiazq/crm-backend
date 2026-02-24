package com.crm.mcsv_user.repository;

import com.crm.mcsv_user.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Role> searchRoles(@Param("search") String search, Pageable pageable);

    @Query(value = "SELECT r FROM Role r WHERE " +
            "('' = :search OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR r.enabled = :status)",
           countQuery = "SELECT COUNT(r) FROM Role r WHERE " +
            "('' = :search OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR r.enabled = :status)")
    Page<Role> filterRoles(@Param("search") String search, @Param("status") Boolean status, Pageable pageable);
}
