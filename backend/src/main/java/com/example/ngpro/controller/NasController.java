package com.example.ngpro.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/nas")
public class NasController {

    @Value("${nas.api.url:http://nas-simulator:4003}")
    private String nasApiUrl;

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = nasApiUrl + "/api/nas/status";
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("online", false);
            error.put("error", "NAS unavailable: " + e.getMessage());
            return error;
        }
    }

    @GetMapping("/sessions")
    public Map<String, Object> getSessions() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = nasApiUrl + "/api/nas/sessions";
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> connect(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password required"));
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> request = new HashMap<>();
            request.put("username", username);
            request.put("password", password);

            String url = nasApiUrl + "/api/nas/connect";
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Connection failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnect(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String sessionId = body.get("sessionId");

        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> request = new HashMap<>();
            if (username != null) request.put("username", username);
            if (sessionId != null) request.put("sessionId", sessionId);

            String url = nasApiUrl + "/api/nas/disconnect";
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Disconnect failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/kick")
    public ResponseEntity<Map<String, Object>> kick(@RequestBody Map<String, String> body) {
        String username = body.get("username");

        if (username == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username required"));
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> request = new HashMap<>();
            request.put("username", username);

            String url = nasApiUrl + "/api/nas/kick";
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Kick failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/test-auth")
    public ResponseEntity<Map<String, Object>> testAuth(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password required"));
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> request = new HashMap<>();
            request.put("username", username);
            request.put("password", password);

            String url = nasApiUrl + "/api/nas/test-auth";
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Test failed: " + e.getMessage()
            ));
        }
    }
}
