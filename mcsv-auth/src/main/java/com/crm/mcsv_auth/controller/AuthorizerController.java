package com.crm.mcsv_auth.controller;

import com.crm.mcsv_auth.dto.ValidationError;
import com.crm.mcsv_auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthorizerController {

    private final AuthService authService;

    @PostMapping("/v1/validateToken")
    public ValidationError validateToken(@RequestParam String jwt) {
        boolean isValid = authService.validateToken(extractToken(jwt));
        if (isValid) {
            return new ValidationError(true, null);
        }
        return new ValidationError(false, "Invalid token");
    }

    @PostMapping("/v1/validateTokenUrl")
    public ValidationError validateTokenUrl(
            @RequestParam String jwt,
            @RequestParam String urlPath,
            @RequestParam String method
    ) {
        boolean isValid = authService.validateToken(extractToken(jwt));
        if (isValid) {
            return new ValidationError(true, null);
        }
        return new ValidationError(false, "Invalid token");
    }

    private String extractToken(String tokenHeader) {
        if (tokenHeader == null) {
            return "";
        }
        if (tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7);
        }
        return tokenHeader;
    }
}
