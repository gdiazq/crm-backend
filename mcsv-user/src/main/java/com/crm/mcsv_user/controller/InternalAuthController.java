package com.crm.mcsv_user.controller;

import com.crm.mcsv_user.dto.UserDTO;
import com.crm.mcsv_user.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
@Slf4j
@Hidden // No aparece en Swagger (endpoint interno)
public class InternalAuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${internal.service.key:internal-microservice-key-2024}")
    private String internalServiceKey;

    @GetMapping("/user/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @PathVariable String username) {

        // Validación básica de seguridad entre microservicios
        if (serviceKey == null || !serviceKey.equals(internalServiceKey)) {
            log.warn("Unauthorized internal service call attempt to getUserByUsername");
            return ResponseEntity.status(403).build();
        }

        log.info("Internal lookup for user by username: {}", username);
        try {
            UserDTO user = userService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error looking up user by username: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/email")
    public ResponseEntity<UserDTO> getUserByEmail(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @RequestParam String email) {

        // Validación básica de seguridad entre microservicios
        if (serviceKey == null || !serviceKey.equals(internalServiceKey)) {
            log.warn("Unauthorized internal service call attempt to getUserByEmail");
            return ResponseEntity.status(403).build();
        }

        log.info("Internal lookup for user by email: {}", email);
        try {
            UserDTO user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error looking up user by email: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/validate-credentials")
    public ResponseEntity<Boolean> validateCredentials(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @RequestBody CredentialsRequest credentials) {

        // Validación básica de seguridad entre microservicios
        if (serviceKey == null || !serviceKey.equals(internalServiceKey)) {
            log.warn("Unauthorized internal service call attempt to validateCredentials");
            return ResponseEntity.status(403).build();
        }

        log.info("Validating credentials for user: {}", credentials.getUsernameOrEmail());
        try {
            // Utilizar el método del servicio que ya implementa toda la lógica de validación
            boolean isValid = userService.validateCredentials(credentials.getUsernameOrEmail(), credentials.getPassword());

            if (isValid) {
                log.info("Credentials validated successfully for user: {}", credentials.getUsernameOrEmail());
            } else {
                log.warn("Credentials validation failed for user: {}", credentials.getUsernameOrEmail());
            }

            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Error validating credentials: {}", e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    // DTO para la solicitud de credenciales
    public static class CredentialsRequest {
        private String usernameOrEmail;
        private String password;

        public String getUsernameOrEmail() {
            return usernameOrEmail;
        }

        public void setUsernameOrEmail(String usernameOrEmail) {
            this.usernameOrEmail = usernameOrEmail;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}