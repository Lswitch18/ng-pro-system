package com.example.ngpro.controller;

import com.example.ngpro.model.Plan;
import com.example.ngpro.repository.PlanRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/plans")
@Validated
public class PlanController {

    @Autowired private PlanRepository planRepo;

    @GetMapping
    public Page<Plan> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return planRepo.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plan> getById(@PathVariable Long id) {
        return planRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Plan> create(@Valid @RequestBody Plan plan) {
        return ResponseEntity.ok(planRepo.save(plan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Plan> update(@PathVariable Long id, @Valid @RequestBody Plan body) {
        return planRepo.findById(id).map(p -> {
            p.setName(body.getName());
            p.setDescription(body.getDescription());
            p.setBasePrice(body.getBasePrice());
            p.setSpeed(body.getSpeed());
            p.setDataCapMb(body.getDataCapMb());
            p.setFamilyPlan(body.isFamilyPlan());
            p.setTier1LimitMb(body.getTier1LimitMb());
            p.setTier2PricePerMb(body.getTier2PricePerMb());
            return ResponseEntity.ok(planRepo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!planRepo.existsById(id)) return ResponseEntity.notFound().build();
        planRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
