package com.crm.mcsv_user.util;

import com.crm.mcsv_user.entity.Permission;
import com.crm.mcsv_user.entity.Role;
import com.crm.mcsv_user.entity.User;
import com.crm.mcsv_user.repository.PermissionRepository;
import com.crm.mcsv_user.repository.RoleRepository;
import com.crm.mcsv_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Data initialization class to add sample users and roles when the application starts up.
 * Only runs when there is no existing data in the database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        initializePermissions();
        initializeRoles();
        assignPermissionsToRoles(); // siempre corre, idempotente
        initializeUsers();

        log.info("Data initialization completed.");
    }

    // -------------------------------------------------------------------------
    // Permisos
    // -------------------------------------------------------------------------

    private void initializePermissions() {
        log.info("Initializing permissions...");

        createPermissionIfNotExists("USER:CREATE", "Crear nuevos usuarios en el sistema");
        createPermissionIfNotExists("USER:READ",   "Ver y consultar información de usuarios");
        createPermissionIfNotExists("USER:UPDATE", "Modificar información de usuarios existentes");
        createPermissionIfNotExists("USER:DELETE", "Eliminar usuarios del sistema");
        createPermissionIfNotExists("ROLE:CREATE", "Crear nuevos roles en el sistema");
        createPermissionIfNotExists("ROLE:READ",   "Ver y consultar información de roles");
        createPermissionIfNotExists("ROLE:UPDATE", "Modificar roles existentes");
        createPermissionIfNotExists("ROLE:DELETE", "Eliminar roles del sistema");
        createPermissionIfNotExists("EMPLOYEE:CREATE", "Crear nuevos empleados en el sistema");
        createPermissionIfNotExists("EMPLOYEE:READ",   "Ver y consultar información de empleados");
        createPermissionIfNotExists("EMPLOYEE:UPDATE", "Modificar información de empleados existentes");
        createPermissionIfNotExists("EMPLOYEE:DELETE", "Deshabilitar empleados del sistema");

        log.info("Permissions initialized.");
    }

    private void createPermissionIfNotExists(String name, String description) {
        if (permissionRepository.findByName(name).isEmpty()) {
            permissionRepository.save(Permission.builder()
                    .name(name)
                    .description(description)
                    .build());
        }
    }

    // -------------------------------------------------------------------------
    // Roles (solo crea si no existen, sin tocar permisos aquí)
    // -------------------------------------------------------------------------

    private void initializeRoles() {
        log.info("Initializing roles...");

        createRoleIfNotExists("ROLE_ADMIN",   "Administrator with full access");
        createRoleIfNotExists("ROLE_USER",    "Regular user with basic permissions");
        createRoleIfNotExists("ROLE_MANAGER", "Manager with intermediate permissions");

        log.info("Roles initialized.");
    }

    private void createRoleIfNotExists(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(Role.builder()
                    .name(name)
                    .description(description)
                    .build());
            log.info("Created role: {}", name);
        }
    }

    // -------------------------------------------------------------------------
    // Asignación de permisos a roles (idempotente: addAll no duplica)
    // -------------------------------------------------------------------------

    private void assignPermissionsToRoles() {
        log.info("Assigning permissions to roles...");

        Permission userCreate = permissionRepository.findByName("USER:CREATE").orElseThrow();
        Permission userRead   = permissionRepository.findByName("USER:READ").orElseThrow();
        Permission userUpdate = permissionRepository.findByName("USER:UPDATE").orElseThrow();
        Permission userDelete = permissionRepository.findByName("USER:DELETE").orElseThrow();
        Permission roleCreate = permissionRepository.findByName("ROLE:CREATE").orElseThrow();
        Permission roleRead   = permissionRepository.findByName("ROLE:READ").orElseThrow();
        Permission roleUpdate = permissionRepository.findByName("ROLE:UPDATE").orElseThrow();
        Permission roleDelete = permissionRepository.findByName("ROLE:DELETE").orElseThrow();

        Permission employeeCreate = permissionRepository.findByName("EMPLOYEE:CREATE").orElseThrow();
        Permission employeeRead   = permissionRepository.findByName("EMPLOYEE:READ").orElseThrow();
        Permission employeeUpdate = permissionRepository.findByName("EMPLOYEE:UPDATE").orElseThrow();
        Permission employeeDelete = permissionRepository.findByName("EMPLOYEE:DELETE").orElseThrow();

        assignToRole("ROLE_ADMIN", new HashSet<>(Set.of(
                userCreate, userRead, userUpdate, userDelete,
                roleCreate, roleRead, roleUpdate, roleDelete,
                employeeCreate, employeeRead, employeeUpdate, employeeDelete)));

        assignToRole("ROLE_MANAGER", new HashSet<>(Set.of(
                userRead, userUpdate,
                employeeCreate, employeeRead, employeeUpdate)));

        assignToRole("ROLE_USER", new HashSet<>(Set.of(userRead, employeeRead)));

        log.info("Permissions assigned to roles.");
    }

    private void assignToRole(String roleName, Set<Permission> permissions) {
        roleRepository.findByName(roleName).ifPresent(role -> {
            role.getPermissions().addAll(permissions);
            roleRepository.save(role);
            log.info("Permissions assigned to {}: {}", roleName,
                    permissions.stream().map(Permission::getName).toList());
        });
    }

    // -------------------------------------------------------------------------
    // Usuarios de muestra
    // -------------------------------------------------------------------------

    private void initializeUsers() {
        log.info("Initializing sample users...");

        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping user initialization.");
            return;
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
        Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                .orElseThrow(() -> new RuntimeException("ROLE_MANAGER not found"));

        Set<Role> adminRoles   = new HashSet<>(Set.of(adminRole));
        Set<Role> userRoles    = new HashSet<>(Set.of(userRole));
        Set<Role> managerRoles = new HashSet<>(Set.of(managerRole, userRole));

        userRepository.save(User.builder()
                .username("admin").email("admin@crm.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("System").lastName("Administrator")
                .phoneNumber("+1111111111")
                .enabled(true).accountNonExpired(true)
                .accountNonLocked(true).credentialsNonExpired(true)
                .roles(adminRoles).build());

        userRepository.save(User.builder()
                .username("johndoe").email("john.doe@crm.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("John").lastName("Doe")
                .phoneNumber("+1234567890")
                .enabled(true).accountNonExpired(true)
                .accountNonLocked(true).credentialsNonExpired(true)
                .roles(userRoles).build());

        userRepository.save(User.builder()
                .username("manager").email("manager@crm.com")
                .password(passwordEncoder.encode("manager123"))
                .firstName("Jane").lastName("Manager")
                .phoneNumber("+1987654321")
                .enabled(true).accountNonExpired(true)
                .accountNonLocked(true).credentialsNonExpired(true)
                .roles(managerRoles).build());

        userRepository.save(User.builder()
                .username("janedoe").email("jane.doe@crm.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Jane").lastName("Doe")
                .phoneNumber("+1122334455")
                .enabled(true).accountNonExpired(true)
                .accountNonLocked(true).credentialsNonExpired(true)
                .roles(userRoles).build());

        userRepository.save(User.builder()
                .username("robertsmith").email("robert.smith@crm.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Robert").lastName("Smith")
                .phoneNumber("+1555123456")
                .enabled(true).accountNonExpired(true)
                .accountNonLocked(true).credentialsNonExpired(true)
                .roles(userRoles).build());

        log.info("Created 5 sample users: admin, johndoe, manager, janedoe, robertsmith");
    }
}
