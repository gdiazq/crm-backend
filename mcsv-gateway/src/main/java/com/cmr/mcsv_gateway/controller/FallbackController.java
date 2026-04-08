package com.cmr.mcsv_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Map<String, String> SERVICE_MESSAGES = Map.of(
            "mcsv-auth", "Auth service is currently unavailable. Please try again later.",
            "mcsv-user", "User service is currently unavailable. Please try again later.",
            "mcsv-role", "Role service is currently unavailable. Please try again later.",
            "mcsv-notification", "Notification service is currently unavailable. Please try again later.",
            "mcsv-storage", "Storage service is currently unavailable. Please try again later.",
            "mcsv-rrhh", "RRHH service is currently unavailable. Please try again later.",
            "mcsv-project", "Project service is currently unavailable. Please try again later."
    );

    @RequestMapping(value = "/{service}", method = {RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public ResponseEntity<Map<String, Object>> fallback(@PathVariable String service) {
        String message = SERVICE_MESSAGES.getOrDefault(service, 
                "Service " + service + " is currently unavailable. Please try again later.");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Service Unavailable",
                "message", message,
                "service", service,
                "timestamp", Instant.now().toString()
        ));
    }
}
