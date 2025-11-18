package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.CreateUserInternalRequest;
import com.crm.mcsv_auth.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mcsv-user")
public interface UserClient {

    @GetMapping("/internal/auth/user/username/{username}")
    ResponseEntity<UserDTO> getUserByUsername(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @PathVariable("username") String username);

    @GetMapping("/internal/auth/user/email")
    ResponseEntity<UserDTO> getUserByEmail(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestParam("email") String email);

    @GetMapping("/internal/users/{id}")
    ResponseEntity<UserDTO> getUserById(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @PathVariable("id") Long id);

    @PostMapping("/internal/users")
    ResponseEntity<UserDTO> createUserInternal(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestBody CreateUserInternalRequest request
    );

    @PostMapping("/internal/auth/validate-credentials")
    ResponseEntity<Boolean> validateCredentials(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestBody CredentialsRequest credentials);

    // DTO para la solicitud de credenciales
    class CredentialsRequest {
        private String usernameOrEmail;
        private String password;

        public CredentialsRequest(String usernameOrEmail, String password) {
            this.usernameOrEmail = usernameOrEmail;
            this.password = password;
        }

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
