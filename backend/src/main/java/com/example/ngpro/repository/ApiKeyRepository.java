package com.example.ngpro.repository;

import com.example.ngpro.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByApiKeyAndStatus(String apiKey, String status);
    Optional<ApiKey> findByClientName(String clientName);
}
