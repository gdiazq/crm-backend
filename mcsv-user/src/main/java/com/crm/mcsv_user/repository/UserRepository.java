package com.crm.mcsv_user.repository;

import com.crm.mcsv_user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
            "('' = :name OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "('' = :email OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:status IS NULL OR u.enabled = :status) AND " +
            "(:roleId IS NULL OR r.id = :roleId)",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u LEFT JOIN u.roles r WHERE " +
            "('' = :name OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "('' = :email OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:status IS NULL OR u.enabled = :status) AND " +
            "(:roleId IS NULL OR r.id = :roleId)")
    Page<User> filterUsers(@Param("name") String name, @Param("email") String email,
                           @Param("status") Boolean status, @Param("roleId") Long roleId,
                           Pageable pageable);

    @Query(value = "SELECT u FROM User u ORDER BY (SELECT MIN(r.name) FROM u.roles r) ASC",
           countQuery = "SELECT COUNT(u) FROM User u")
    Page<User> findAllSortedByRoleAsc(Pageable pageable);

    @Query(value = "SELECT u FROM User u ORDER BY (SELECT MIN(r.name) FROM u.roles r) DESC",
           countQuery = "SELECT COUNT(u) FROM User u")
    Page<User> findAllSortedByRoleDesc(Pageable pageable);

    @Query(value = "SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "ORDER BY (SELECT MIN(r.name) FROM u.roles r) ASC",
           countQuery = "SELECT COUNT(u) FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsersSortedByRoleAsc(@Param("search") String search, Pageable pageable);

    @Query(value = "SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "ORDER BY (SELECT MIN(r.name) FROM u.roles r) DESC",
           countQuery = "SELECT COUNT(u) FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsersSortedByRoleDesc(@Param("search") String search, Pageable pageable);

    List<User> findAllByRolesId(Long roleId);

    long countByEnabled(boolean enabled);
}
