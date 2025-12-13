package com.quiz.controller;

import com.quiz.dto.AdminLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AdminLoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (adminUsername.equals(request.getUsername()) &&
                adminPassword.equals(request.getPassword())) {

            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", "admin-token-" + System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Boolean>> verifyToken(@RequestHeader("Authorization") String token) {
        Map<String, Boolean> response = new HashMap<>();
        // Simple token check - in production, use JWT
        response.put("valid", token != null && token.startsWith("admin-token-"));
        return ResponseEntity.ok(response);
    }
}
