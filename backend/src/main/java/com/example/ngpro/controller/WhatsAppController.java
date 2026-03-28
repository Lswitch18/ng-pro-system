package com.example.ngpro.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    @Value("${whatsapp.api.url:http://whatsapp-bot:4002}")
    private String whatsappApiUrl;

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = whatsappApiUrl + "/api/status";
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("connected", false);
            error.put("message", "WhatsApp bot unavailable: " + e.getMessage());
            return error;
        }
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String message = body.get("message");

        if (phone == null || message == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phone and message are required"));
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> request = new HashMap<>();
            request.put("phone", phone);
            request.put("message", message);

            String url = whatsappApiUrl + "/api/send";
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to send WhatsApp message",
                "details", e.getMessage()
            ));
        }
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<Map<String, Object>> sendBulkMessages(@RequestBody Map<String, Object> body) {
        List<Map<String, String>> recipients = (List<Map<String, String>>) body.get("recipients");
        String template = (String) body.getOrDefault("template", "default");

        if (recipients == null || recipients.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Recipients list is required"));
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            String defaultMessage = "NG-PRO Enterprise: Your invoice is overdue. Please access http://localhost:3001 for details.";
            
            recipients.forEach(r -> r.put("message", template.equals("collection") ? 
                "NG-PRO Enterprise: Your invoice is overdue. Amount: R$ " + r.getOrDefault("amount", "0") + ". Please make payment to avoid service suspension." : 
                defaultMessage));

            Map<String, Object> request = new HashMap<>();
            request.put("recipients", recipients);

            String url = whatsappApiUrl + "/api/send-bulk";
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to send bulk WhatsApp messages",
                "details", e.getMessage()
            ));
        }
    }
}
