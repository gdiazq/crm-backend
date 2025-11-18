package com.crm.mcsv_user.controller;

import com.crm.mcsv_user.dto.CreateUserRequest;
import com.crm.mcsv_user.dto.UserResponse;
import com.crm.mcsv_user.dto.UserDTO;
import com.crm.mcsv_user.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
@Hidden // No aparece en Swagger (endpoint interno)
public class InternalUserController {

    private final UserService userService;

    @Value("${internal.service.key:internal-microservice-key-2024}")
    private String internalServiceKey;

    @PostMapping
    public ResponseEntity<UserResponse> createUserInternal(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @Valid @RequestBody CreateUserRequest request) {

        // Validación básica de seguridad entre microservicios
        if (serviceKey == null || !serviceKey.equals(internalServiceKey)) {
            log.warn("Unauthorized internal service call attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Creating user via internal endpoint: {}", request.getUsername());
        UserResponse createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @PathVariable Long id) {

        // Validación básica de seguridad entre microservicios
        if (serviceKey == null || !serviceKey.equals(internalServiceKey)) {
            log.warn("Unauthorized internal service call attempt to getUserById");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Internal lookup for user by ID: {}", id);
        try {
            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error looking up user by ID: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
