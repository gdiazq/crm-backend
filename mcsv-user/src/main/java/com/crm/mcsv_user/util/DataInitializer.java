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
        initializePermissions();
        initializeRoles();
        assignPermissionsToRoles();
        initializeUsers();
    }

    // -------------------------------------------------------------------------
    // Permisos
    // -------------------------------------------------------------------------

    private void initializePermissions() {
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
        createPermissionIfNotExists("HR_REQUEST:READ",    "Ver y consultar solicitudes RRHH");
        createPermissionIfNotExists("HR_REQUEST:APPROVE", "Aprobar solicitudes RRHH");
        createPermissionIfNotExists("HR_REQUEST:REJECT",  "Rechazar solicitudes RRHH");
        createPermissionIfNotExists("CONTRACT:CREATE", "Crear contratos laborales");
        createPermissionIfNotExists("CONTRACT:READ",   "Ver y consultar contratos laborales");
        createPermissionIfNotExists("CONTRACT:UPDATE", "Modificar contratos laborales");
        createPermissionIfNotExists("TRANSFER:CREATE", "Crear traspasos de centro de costo");
        createPermissionIfNotExists("TRANSFER:READ",   "Ver y consultar traspasos de centro de costo");
        createPermissionIfNotExists("TRANSFER:UPDATE", "Modificar traspasos de centro de costo");
        createPermissionIfNotExists("TRANSFER:DELETE", "Eliminar documentos asociados a traspasos");
        createPermissionIfNotExists("ANNEX:CREATE", "Crear anexos de contrato");
        createPermissionIfNotExists("ANNEX:READ",   "Ver y consultar anexos de contrato");
        createPermissionIfNotExists("ANNEX:UPDATE", "Modificar anexos de contrato");
        createPermissionIfNotExists("ANNEX:DELETE", "Eliminar documentos asociados a anexos");
        createPermissionIfNotExists("PROJECT_TYPE:CREATE", "Crear tipos de proyecto");
        createPermissionIfNotExists("PROJECT_TYPE:READ",   "Ver y consultar tipos de proyecto");
        createPermissionIfNotExists("PROJECT_TYPE:UPDATE", "Modificar tipos de proyecto");
        createPermissionIfNotExists("PROJECT_TYPE:DELETE", "Eliminar tipos de proyecto");
        createPermissionIfNotExists("PROJECT_STATUS:CREATE", "Crear estados de proyecto");
        createPermissionIfNotExists("PROJECT_STATUS:READ",   "Ver y consultar estados de proyecto");
        createPermissionIfNotExists("PROJECT_STATUS:UPDATE", "Modificar estados de proyecto");
        createPermissionIfNotExists("PROJECT_STATUS:DELETE", "Eliminar estados de proyecto");
        createPermissionIfNotExists("PROJECT_SPECIALTY:CREATE", "Crear especialidades de proyecto");
        createPermissionIfNotExists("PROJECT_SPECIALTY:READ",   "Ver y consultar especialidades de proyecto");
        createPermissionIfNotExists("PROJECT_SPECIALTY:UPDATE", "Modificar especialidades de proyecto");
        createPermissionIfNotExists("PROJECT_SPECIALTY:DELETE", "Eliminar especialidades de proyecto");
        createPermissionIfNotExists("PROJECT:CREATE", "Crear proyectos");
        createPermissionIfNotExists("PROJECT:READ",   "Ver y consultar proyectos");
        createPermissionIfNotExists("PROJECT:UPDATE", "Modificar proyectos");
        createPermissionIfNotExists("PROJECT:DELETE", "Eliminar proyectos");
        createPermissionIfNotExists("LEGAL_TERMINATION_CAUSE:CREATE", "Crear causas legales de terminación");
        createPermissionIfNotExists("LEGAL_TERMINATION_CAUSE:READ",   "Ver y consultar causas legales de terminación");
        createPermissionIfNotExists("LEGAL_TERMINATION_CAUSE:UPDATE", "Modificar causas legales de terminación");
        createPermissionIfNotExists("LEGAL_TERMINATION_CAUSE:DELETE", "Eliminar causas legales de terminación");
        createPermissionIfNotExists("QUALITY_OF_WORK:CREATE", "Crear categorías de calidad de trabajo");
        createPermissionIfNotExists("QUALITY_OF_WORK:READ",   "Ver y consultar categorías de calidad de trabajo");
        createPermissionIfNotExists("QUALITY_OF_WORK:UPDATE", "Modificar categorías de calidad de trabajo");
        createPermissionIfNotExists("QUALITY_OF_WORK:DELETE", "Eliminar categorías de calidad de trabajo");
        createPermissionIfNotExists("SAFETY_COMPLIANCE:CREATE", "Crear categorías de cumplimiento de seguridad");
        createPermissionIfNotExists("SAFETY_COMPLIANCE:READ",   "Ver y consultar categorías de cumplimiento de seguridad");
        createPermissionIfNotExists("SAFETY_COMPLIANCE:UPDATE", "Modificar categorías de cumplimiento de seguridad");
        createPermissionIfNotExists("SAFETY_COMPLIANCE:DELETE", "Eliminar categorías de cumplimiento de seguridad");
        createPermissionIfNotExists("NO_RE_HIRED_CAUSE:CREATE", "Crear causas de no recontratación");
        createPermissionIfNotExists("NO_RE_HIRED_CAUSE:READ",   "Ver y consultar causas de no recontratación");
        createPermissionIfNotExists("NO_RE_HIRED_CAUSE:UPDATE", "Modificar causas de no recontratación");
        createPermissionIfNotExists("NO_RE_HIRED_CAUSE:DELETE", "Eliminar causas de no recontratación");
        createPermissionIfNotExists("TERMINATION:CREATE", "Crear finiquitos");
        createPermissionIfNotExists("TERMINATION:READ",   "Ver y consultar finiquitos");
        createPermissionIfNotExists("TERMINATION:UPDATE", "Modificar finiquitos");
        createPermissionIfNotExists("TERMINATION:DELETE", "Eliminar finiquitos");
        createPermissionIfNotExists("TERMINATION_QUIZ_QUESTION:CREATE", "Crear preguntas del cuestionario de finiquito");
        createPermissionIfNotExists("TERMINATION_QUIZ_QUESTION:READ",   "Ver y consultar preguntas del cuestionario de finiquito");
        createPermissionIfNotExists("TERMINATION_QUIZ_QUESTION:UPDATE", "Modificar preguntas del cuestionario de finiquito");
        createPermissionIfNotExists("TERMINATION_QUIZ_QUESTION:DELETE", "Eliminar preguntas del cuestionario de finiquito");
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
        if (roleRepository.count() > 0) return;

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
        if (roleRepository.findByNameWithPermissions("ROLE_ADMIN").map(r -> !r.getPermissions().isEmpty()).orElse(false)) return;

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
        Permission hrRequestRead    = permissionRepository.findByName("HR_REQUEST:READ").orElseThrow();
        Permission hrRequestApprove = permissionRepository.findByName("HR_REQUEST:APPROVE").orElseThrow();
        Permission hrRequestReject  = permissionRepository.findByName("HR_REQUEST:REJECT").orElseThrow();

        assignToRole("ROLE_ADMIN", new HashSet<>(Set.of(
                userCreate, userRead, userUpdate, userDelete,
                roleCreate, roleRead, roleUpdate, roleDelete,
                employeeCreate, employeeRead, employeeUpdate, employeeDelete,
                hrRequestRead, hrRequestApprove, hrRequestReject)));

        assignToRole("ROLE_MANAGER", new HashSet<>(Set.of(
                userRead, userUpdate,
                employeeCreate, employeeRead, employeeUpdate,
                hrRequestRead, hrRequestApprove, hrRequestReject)));

        assignToRole("ROLE_USER", new HashSet<>(Set.of(userRead, employeeRead, hrRequestRead)));

        log.info("Permissions assigned to roles.");
    }

    private void assignToRole(String roleName, Set<Permission> permissions) {
        roleRepository.findByNameWithPermissions(roleName).ifPresent(role -> {
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
        if (userRepository.count() > 0) return;

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
