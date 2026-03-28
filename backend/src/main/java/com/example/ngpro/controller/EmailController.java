package com.example.ngpro.controller;

import com.example.ngpro.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/email")
    public ResponseEntity<?> testEmail(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        String name = body.get("name");
        
        if (to == null || to.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Parâmetro 'to' é obrigatório"));
        }
        
        emailService.sendWelcomeEmail(to, name != null ? name : "Cliente");
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "E-mail enviado para " + to,
            "mailpit", "Acesse http://localhost:8025 para visualizar"
        ));
    }
}
