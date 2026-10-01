package com.example.secretcase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${app.admin-name}")
    private String adminName;
    @Value("${app.admin-password}")
    private String adminPassword;
    private final String WWW_AUTHENTICATE_VALUE = "Basic realm=\"secret-case\"";
    private final String UNAUTHORIZED = "Unauthorized";
    private final String INVALID_CREDENTIALS = "Invalid credentials";
    private final String SUCCESS_MESSAGE = "Hello, welcome to the secret case!";
    private final int CREDENTIAL_PARTS = 2;

    @GetMapping("/")
    public ResponseEntity<String> hello(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            responseHelper(UNAUTHORIZED);
        }

        String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
        String[] parts = decoded.split(":", CREDENTIAL_PARTS);

        if (parts.length != CREDENTIAL_PARTS) {
            responseHelper(INVALID_CREDENTIALS);
        }

        String enteredName = parts[0];
        String enteredPassword = parts[1];

        if (!adminName.equals(enteredName)) {
            responseHelper(INVALID_CREDENTIALS);
        }
        if (!MessageDigest.isEqual(
            adminPassword.getBytes(StandardCharsets.UTF_8), 
            enteredPassword.getBytes(StandardCharsets.UTF_8))) {
            responseHelper(INVALID_CREDENTIALS);
        }
        return ResponseEntity.ok(SUCCESS_MESSAGE);
    }

    private ResponseEntity<String> responseHelper(String body) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, WWW_AUTHENTICATE_VALUE)
                    .body(body);
    }
}
