package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.CreateUserInternalRequest;
import com.crm.mcsv_auth.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mcsv-user")
public interface UserClient {

    @GetMapping("/detail/username/{username}")
    ResponseEntity<UserDTO> getUserByUsername(
            @PathVariable("username") String username);

    @GetMapping("/detail/email")
    ResponseEntity<UserDTO> getUserByEmail(
            @RequestParam("email") String email);

    @GetMapping("/detail/{id}")
    ResponseEntity<UserDTO> getUserById(
            @PathVariable("id") Long id);

    @PostMapping("/sign-up")
    ResponseEntity<UserDTO> signUpUser(@RequestBody CreateUserInternalRequest request);

    @PostMapping("/validate-credentials")
    ResponseEntity<Boolean> validateCredentials(
            @RequestBody CredentialsRequest credentials);

    @PostMapping("/update-password")
    ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest request);

    @PutMapping("/{id}/verify-email")
    ResponseEntity<Void> verifyEmail(@PathVariable("id") Long id);

    @PutMapping("/{id}/last-login")
    ResponseEntity<Void> updateLastLogin(@PathVariable("id") Long id);

    class UpdatePasswordRequest {
        private Long userId;
        private String newPassword;

        public UpdatePasswordRequest(Long userId, String newPassword) {
            this.userId = userId;
            this.newPassword = newPassword;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

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
