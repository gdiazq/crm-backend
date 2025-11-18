package com.crm.mcsv_user.util;

import com.crm.mcsv_user.entity.Role;
import com.crm.mcsv_user.entity.User;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        
        // Initialize roles if they don't exist
        initializeRoles();
        
        // Initialize users if they don't exist
        initializeUsers();
        
        log.info("Data initialization completed.");
    }

    private void initializeRoles() {
        log.info("Initializing roles...");

        // Check if roles already exist to prevent duplicates
        if (roleRepository.count() == 0) {
            // Create common roles
            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Administrator with full access")
                    .build();
            
            Role userRole = Role.builder()
                    .name("ROLE_USER")
                    .description("Regular user with basic permissions")
                    .build();
            
            Role managerRole = Role.builder()
                    .name("ROLE_MANAGER")
                    .description("Manager with intermediate permissions")
                    .build();

            roleRepository.save(adminRole);
            roleRepository.save(userRole);
            roleRepository.save(managerRole);

            log.info("Created 3 roles: ROLE_ADMIN, ROLE_USER, ROLE_MANAGER");
        } else {
            log.info("Roles already exist, skipping role initialization.");
        }
    }

    private void initializeUsers() {
        log.info("Initializing sample users...");

        // Check if users already exist to prevent duplicates
        if (userRepository.count() == 0) {
            // Get the roles we created
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
            Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                    .orElseThrow(() -> new RuntimeException("ROLE_MANAGER not found"));

            // Create sample users with different roles
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);

            Set<Role> userRoles = new HashSet<>();
            userRoles.add(userRole);

            Set<Role> managerRoles = new HashSet<>();
            managerRoles.add(managerRole);
            managerRoles.add(userRole); // Managers also have user permissions

            // Admin user
            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@crm.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .phoneNumber("+1111111111")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(adminRoles)
                    .build();

            // Regular user
            User regularUser = User.builder()
                    .username("johndoe")
                    .email("john.doe@crm.com")
                    .password(passwordEncoder.encode("password123"))
                    .firstName("John")
                    .lastName("Doe")
                    .phoneNumber("+1234567890")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(userRoles)
                    .build();

            // Manager user
            User managerUser = User.builder()
                    .username("manager")
                    .email("manager@crm.com")
                    .password(passwordEncoder.encode("manager123"))
                    .firstName("Jane")
                    .lastName("Manager")
                    .phoneNumber("+1987654321")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(managerRoles)
                    .build();

            // Additional sample users
            User user2 = User.builder()
                    .username("janedoe")
                    .email("jane.doe@crm.com")
                    .password(passwordEncoder.encode("password123"))
                    .firstName("Jane")
                    .lastName("Doe")
                    .phoneNumber("+1122334455")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(userRoles)
                    .build();

            User user3 = User.builder()
                    .username("robertsmith")
                    .email("robert.smith@crm.com")
                    .password(passwordEncoder.encode("password123"))
                    .firstName("Robert")
                    .lastName("Smith")
                    .phoneNumber("+1555123456")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(userRoles)
                    .build();

            userRepository.save(adminUser);
            userRepository.save(regularUser);
            userRepository.save(managerUser);
            userRepository.save(user2);
            userRepository.save(user3);

            log.info("Created 5 sample users: admin, johndoe, manager, janedoe, robertsmith");
        } else {
            log.info("Users already exist, skipping user initialization.");
        }
    }
}