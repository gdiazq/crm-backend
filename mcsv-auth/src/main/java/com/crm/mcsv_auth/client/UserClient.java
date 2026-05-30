package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.CreateUserInternalRequest;
import com.crm.mcsv_auth.dto.CredentialsRequest;
import com.crm.mcsv_auth.dto.UpdatePasswordRequest;
import com.crm.mcsv_auth.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
@FeignClient(name = "mcsv-user", contextId = "authUserClient")
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

    @PutMapping("/{id}/avatar-url")
    ResponseEntity<Void> updateAvatarUrl(@PathVariable("id") Long id, @RequestBody java.util.Map<String, String> body);

    @PostMapping("/{id}/verify-email-code")
    ResponseEntity<Boolean> validateAndConsumeCode(
            @PathVariable("id") Long userId,
            @RequestBody java.util.Map<String, String> body);
}
