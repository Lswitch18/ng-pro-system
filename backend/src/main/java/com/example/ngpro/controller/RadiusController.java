package com.example.ngpro.controller;

import com.example.ngpro.model.Customer;
import com.example.ngpro.model.RadCheck;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.repository.RadCheckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/radius")
public class RadiusController {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private RadCheckRepository radCheckRepo;

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return radCheckRepo.findAllUsers();
    }

    @GetMapping("/users/{username}")
    public Map<String, Object> getUser(@PathVariable String username) {
        return radCheckRepo.findUserByUsername(username);
    }

    @PostMapping("/users/sync")
    public ResponseEntity<Map<String, Object>> syncUsersFromBilling() {
        List<Customer> activeCustomers = customerRepo.findByStatus("ACTIVE");
        
        int synced = 0;
        int updated = 0;
        
        for (Customer customer : activeCustomers) {
            if (customer.getEmail() == null || customer.getEmail().isEmpty()) continue;
            
            String username = customer.getEmail().split("@")[0];
            
            Optional<RadCheck> existing = radCheckRepo.findByUsername(username);
            
            if (existing.isEmpty()) {
                RadCheck newUser = new RadCheck();
                newUser.setUsername(username);
                newUser.setAttribute("Cleartext-Password");
                newUser.setOp(":=");
                newUser.setValue("ngpro" + customer.getId());
                radCheckRepo.save(newUser);
                synced++;
            } else {
                updated++;
            }
        }

        return ResponseEntity.ok(Map.of(
            "synced", synced,
            "updated", updated,
            "total", activeCustomers.size(),
            "message", synced + " users synced from billing"
        ));
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Optional<RadCheck> existing = radCheckRepo.findByUsername(username);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User already exists"));
        }

        RadCheck newUser = new RadCheck();
        newUser.setUsername(username);
        newUser.setAttribute("Cleartext-Password");
        newUser.setOp(":=");
        newUser.setValue(password);
        radCheckRepo.save(newUser);

        return ResponseEntity.ok(Map.of(
            "username", username,
            "message", "User created successfully"
        ));
    }

    @PutMapping("/users/{username}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable String username, @RequestBody Map<String, String> body) {
        String password = body.get("password");
        
        Optional<RadCheck> existing = radCheckRepo.findByUsername(username);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RadCheck user = existing.get();
        user.setValue(password);
        radCheckRepo.save(user);

        return ResponseEntity.ok(Map.of(
            "username", username,
            "message", "Password updated successfully"
        ));
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String username) {
        Optional<RadCheck> existing = radCheckRepo.findByUsername(username);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        radCheckRepo.delete(existing.get());

        return ResponseEntity.ok(Map.of(
            "username", username,
            "message", "User deleted successfully"
        ));
    }

    @PostMapping("/users/{username}/enable")
    public ResponseEntity<Map<String, Object>> enableUser(@PathVariable String username) {
        return updateUserAttribute(username, "Auth-Type", "Accept");
    }

    @PostMapping("/users/{username}/disable")
    public ResponseEntity<Map<String, Object>> disableUser(@PathVariable String username) {
        return updateUserAttribute(username, "Auth-Type", "Reject");
    }

    private ResponseEntity<Map<String, Object>> updateUserAttribute(String username, String attribute, String value) {
        return ResponseEntity.ok(Map.of(
            "username", username,
            "attribute", attribute,
            "value", value,
            "message", "User attribute updated"
        ));
    }

    @GetMapping("/accounting")
    public List<Map<String, Object>> getAccounting(@RequestParam(required = false) String username,
                                                     @RequestParam(defaultValue = "100") int limit) {
        if (username != null && !username.isEmpty()) {
            return radCheckRepo.getAccountingByUsername(username, limit);
        }
        return radCheckRepo.getRecentAccounting(limit);
    }

    @GetMapping("/nas")
    public List<Map<String, Object>> getNAS() {
        return radCheckRepo.getAllNAS();
    }

    @PostMapping("/nas")
    public ResponseEntity<Map<String, Object>> addNAS(@RequestBody Map<String, String> body) {
        String nasname = body.get("nasname");
        String shortname = body.get("shortname");
        String secret = body.get("secret");
        
        if (nasname == null || secret == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "nasname and secret are required"));
        }

        return ResponseEntity.ok(Map.of(
            "nasname", nasname,
            "shortname", shortname != null ? shortname : nasname,
            "secret", secret,
            "message", "NAS configured"
        ));
    }
}
