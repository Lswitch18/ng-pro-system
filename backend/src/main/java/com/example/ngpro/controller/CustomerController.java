package com.example.ngpro.controller;

import com.example.ngpro.model.Customer;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@Validated
@Tag(name = "Clientes", description = "API para gerenciamento de clientes")
public class CustomerController {

    @Autowired private CustomerRepository customerRepo;
    @Autowired private EmailService emailService;

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna todos os clientes com paginação")
    public Page<Customer> getAll(@Parameter(description = "Configurações de paginação") @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return customerRepo.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna um cliente específico pelo ID")
    public ResponseEntity<Customer> getById(@Parameter(description = "ID do cliente") @PathVariable Long id) {
        return customerRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Criar cliente", description = "Cria um novo cliente no sistema")
    public ResponseEntity<Customer> create(@Valid @RequestBody Customer customer) {
        Customer saved = customerRepo.save(customer);
        if (saved.getEmail() != null && !saved.getEmail().isEmpty()) {
            emailService.sendWelcomeEmail(saved.getEmail(), saved.getName());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente")
    public ResponseEntity<Customer> update(@Parameter(description = "ID do cliente") @PathVariable Long id, @Valid @RequestBody Customer body) {
        return customerRepo.findById(id).map(c -> {
            c.setName(body.getName());
            c.setEmail(body.getEmail());
            c.setPhone(body.getPhone());
            c.setStatus(body.getStatus());
            c.setAddress(body.getAddress());
            c.setCity(body.getCity());
            c.setState(body.getState());
            c.setPlanId(body.getPlanId());
            return ResponseEntity.ok(customerRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente", description = "Remove um cliente do sistema")
    public ResponseEntity<Void> delete(@Parameter(description = "ID do cliente") @PathVariable Long id) {
        if (!customerRepo.existsById(id)) return ResponseEntity.notFound().build();
        customerRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status", description = "Atualiza o status de um cliente")
    public ResponseEntity<Customer> updateStatus(@Parameter(description = "ID do cliente") @PathVariable Long id, @RequestBody Map<String, String> body) {
        return customerRepo.findById(id).map(c -> {
            c.setStatus(body.get("status"));
            return ResponseEntity.ok(customerRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar clientes", description = "Busca clientes por nome ou email")
    public ResponseEntity<List<Customer>> search(@Parameter(description = "Termo de busca") @RequestParam String q) {
        List<Customer> results = customerRepo.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q);
        return ResponseEntity.ok(results);
    }
}
