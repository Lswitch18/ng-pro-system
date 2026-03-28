package com.example.ngpro.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsageEvent {
    private Long customerId;
    private String resourceType; // DATA, VOICE, SMS
    private double amount; // MBs, Minutes, etc.
    private LocalDateTime timestamp;
}
