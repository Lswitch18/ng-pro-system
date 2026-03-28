package com.example.ngpro.repository;

import com.example.ngpro.model.RadCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface RadCheckRepository extends JpaRepository<RadCheck, Long> {

    @Query(value = "SELECT u.username, c.value as password FROM radcheck c " +
           "JOIN (SELECT username, MIN(id) as min_id FROM radcheck GROUP BY username) u " +
           "ON c.username = u.username AND c.id = u.min_id " +
           "WHERE c.attribute = 'Cleartext-Password'", nativeQuery = true)
    List<Map<String, Object>> findAllUsers();

    @Query(value = "SELECT * FROM radcheck WHERE username = :username AND attribute = 'Cleartext-Password'", nativeQuery = true)
    Optional<RadCheck> findByUsername(String username);

    @Query(value = "SELECT u.username, c.value as password FROM radcheck c " +
           "JOIN (SELECT username, MIN(id) as min_id FROM radcheck GROUP BY username) u " +
           "ON c.username = u.username AND c.id = u.min_id " +
           "WHERE c.attribute = 'Cleartext-Password' AND u.username = :username", nativeQuery = true)
    Map<String, Object> findUserByUsername(String username);

    @Query(value = "SELECT * FROM radacct WHERE username = :username ORDER BY acctstarttime DESC LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> getAccountingByUsername(String username, int limit);

    @Query(value = "SELECT * FROM radacct ORDER BY acctstarttime DESC LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> getRecentAccounting(int limit);

    @Query(value = "SELECT * FROM nas", nativeQuery = true)
    List<Map<String, Object>> getAllNAS();
}
