package com.example.ngpro.service;

import com.example.ngpro.model.ApiKey;
import com.example.ngpro.repository.ApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ApiKeyService {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    public ApiKey generateApiKey(String clientName, String clientType, String permissions) {
        String apiKey = "ngpro_" + UUID.randomUUID().toString().replace("-", "");
        
        ApiKey key = new ApiKey();
        key.setClientName(clientName);
        key.setApiKey(apiKey);
        key.setClientType(clientType);
        key.setPermissions(permissions);
        key.setStatus("ACTIVE");
        key.setExpiresAt(LocalDateTime.now().plusYears(1));
        
        return apiKeyRepository.save(key);
    }

    public Optional<ApiKey> validateApiKey(String apiKey) {
        Optional<ApiKey> keyOpt = apiKeyRepository.findByApiKeyAndStatus(apiKey, "ACTIVE");
        
        keyOpt.ifPresent(key -> {
            if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
                key.setStatus("EXPIRED");
                apiKeyRepository.save(key);
                log.warn("API Key expired: {}", key.getClientName());
            } else {
                key.setLastUsed(LocalDateTime.now());
                apiKeyRepository.save(key);
            }
        });
        
        return keyOpt;
    }

    public boolean hasPermission(ApiKey apiKey, String permission) {
        if (apiKey.getPermissions() == null) return false;
        return apiKey.getPermissions().toLowerCase().contains(permission.toLowerCase());
    }

    public void revokeApiKey(Long id) {
        apiKeyRepository.findById(id).ifPresent(key -> {
            key.setStatus("REVOKED");
            apiKeyRepository.save(key);
            log.info("API Key revoked: {}", key.getClientName());
        });
    }
}
