package com.example.ngpro.service;

import com.example.ngpro.model.ApiKey;
import com.example.ngpro.model.AppUser;
import com.example.ngpro.model.Customer;
import com.example.ngpro.model.Invoice;
import com.example.ngpro.model.Plan;
import com.example.ngpro.repository.ApiKeyRepository;
import com.example.ngpro.repository.AppUserRepository;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.repository.InvoiceRepository;
import com.example.ngpro.repository.PlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired private AppUserRepository userRepo;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private PlanRepository planRepo;
    @Autowired private InvoiceRepository invoiceRepo;
    @Autowired private ApiKeyRepository apiKeyRepo;
    @Autowired private PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        // Garantir admin com senha solicitada
        AppUser admin = userRepo.findByUsername("admin").orElse(new AppUser());
        admin.setUsername("admin");
        admin.setPasswordHash(encoder.encode("M3un0m3@@1990"));
        admin.setRole("ADMIN");
        userRepo.save(admin);

        AppUser op = userRepo.findByUsername("operador").orElse(new AppUser());
        op.setUsername("operador");
        op.setPasswordHash(encoder.encode("op123"));
        op.setRole("OPERATOR");
        userRepo.save(op);

        AppUser test = userRepo.findByUsername("teste").orElse(new AppUser());
        test.setUsername("teste");
        test.setPasswordHash(encoder.encode("M3un0m3@@1990"));
        test.setRole("OPERATOR");
        userRepo.save(test);

        // Listar usuários para o usuário validar (Sempre logar no início)
        log.info("=== LISTA DE USUÁRIOS NO BANCO ===");
        userRepo.findAll().forEach(u -> log.info("User: {} | Pass: {} | Role: {}", u.getUsername(), u.getPasswordHash(), u.getRole()));
        log.info("==================================");

        // API Keys padrão para testes
        if (apiKeyRepo.count() == 0) {
            ApiKey prestashopKey = new ApiKey();
            prestashopKey.setClientName("Prestashop Integration");
            prestashopKey.setApiKey("ngpro_prestashop_test_key_2026");
            prestashopKey.setClientType("PRESTASHOP");
            prestashopKey.setPermissions("customers,invoices,plans,collections,apikeys");
            prestashopKey.setStatus("ACTIVE");
            apiKeyRepo.save(prestashopKey);
            log.info("=== API KEY PRESTASHOP ===");
            log.info("Client: Prestashop Integration");
            log.info("API Key: ngpro_prestashop_test_key_2026");
            log.info("Permissions: customers,invoices,plans,collections,apikeys");
            log.info("============================");
        }

        if (userRepo.count() > 3) {
            log.info("Data already initialized. Admin, Operador and Teste users synced.");
            return;
        }

        log.info("Initializing remaining ng-pro seed data...");

        // Plans (ISP / Telecom inspired)
        Plan p1 = plan("Ligga 100M", "Internet 100Mbps", 79.90, "100 Mbps", 0, false, 0, 0.0);
        Plan p2 = plan("Ligga 500M", "Internet 500Mbps Premium", 129.90, "500 Mbps", 0, false, 0, 0.0);
        Plan p3 = plan("Vero Família 1Gbps", "Internet 1Gbps para famílias", 199.90, "1 Gbps", 500_000, true, 400_000, 0.0002);
        Plan p4 = plan("Vero Business Pro", "Solução empresarial dedicada", 349.90, "10 Gbps", 0, false, 0, 0.0);
        planRepo.saveAll(List.of(p1, p2, p3, p4));

        // Customers
        List<Customer> customers = List.of(
            customer("João Silva",       "joao.silva@email.com",   "(41) 99999-1111", "123.456.789-00", "ACTIVE",    p2.getId(), "Rua das Flores, 123", "Curitiba",      "PR", 180_000),
            customer("Maria Souza",      "maria.souza@email.com",  "(41) 98888-2222", "234.567.890-11", "ACTIVE",    p1.getId(), "Av. Batel, 456",      "Curitiba",      "PR", 60_000),
            customer("Carlos Ferreira",  "carlos.f@email.com",     "(11) 97777-3333", "345.678.901-22", "ACTIVE",    p3.getId(), "Rua Augusta, 789",   "São Paulo",     "SP", 520_000),
            customer("Ana Lima",         "ana.lima@email.com",     "(11) 96666-4444", "456.789.012-33", "SUSPENDED", p1.getId(), "Av. Paulista, 1001",  "São Paulo",     "SP", 0),
            customer("Pedro Martins",    "pedro.m@email.com",      "(51) 95555-5555", "567.890.123-44", "ACTIVE",    p4.getId(), "Parque Novo Mundo, 50","Porto Alegre", "RS", 0),
            customer("Lucia Costa",      "lucia.c@email.com",      "(41) 94444-6666", "678.901.234-55", "ACTIVE",    p2.getId(), "Rua XV, 888",         "Curitiba",      "PR", 210_000),
            customer("Rafael Alves",     "rafael.a@email.com",     "(62) 93333-7777", "789.012.345-66", "BLOCKED",   p1.getId(), "Setor Sul, 200",      "Goiânia",       "GO", 0),
            customer("Fernanda Rocha",   "fernanda.r@email.com",   "(21) 92222-8888", "890.123.456-77", "ACTIVE",    p3.getId(), "Copacabana, 500",     "Rio de Janeiro","RJ", 390_000)
        );
        customerRepo.saveAll(customers);

        // Sample invoices
        String refMonth = "2026-02";
        int i = 1;
        for (Customer c : customers) {
            if (!"ACTIVE".equals(c.getStatus())) continue;
            Invoice inv = new Invoice();
            inv.setCustomerId(c.getId());
            inv.setPlanId(c.getPlanId());
            inv.setReferenceMonth(refMonth);
            inv.setBillingProcessId("SEED_INIT_001");
            inv.setDueDate(LocalDateTime.of(2026, 3, 10, 0, 0));
            double base = getPlanBase(planRepo, c.getPlanId());
            inv.setBaseAmount(base);
            inv.setOverageAmount(i % 3 == 0 ? 15.50 : 0.0);
            inv.setTotalAmount(base + inv.getOverageAmount());
            inv.setStatus(i % 2 == 0 ? "PAID" : "PENDING");
            if ("PAID".equals(inv.getStatus())) inv.setPaidAt(LocalDateTime.of(2026, 3, 5, 10, 0));
            invoiceRepo.save(inv);
            i++;
        }
        log.info("ng-pro seed data initialized: 3 users, 4 plans, {} customers, invoices created.", customers.size());
    }

    private Plan plan(String name, String desc, double price, String speed,
                      long capMb, boolean family, long tier1Mb, double tier2Price) {
        Plan p = new Plan();
        p.setName(name); p.setDescription(desc); p.setBasePrice(price);
        p.setSpeed(speed); p.setDataCapMb(capMb); p.setFamilyPlan(family);
        p.setTier1LimitMb(tier1Mb); p.setTier2PricePerMb(tier2Price);
        return p;
    }

    private Customer customer(String name, String email, String phone, String cpf,
                              String status, Long planId, String address, String city,
                              String state, double usageMb) {
        Customer c = new Customer();
        c.setName(name); c.setEmail(email); c.setPhone(phone); c.setCpfCnpj(cpf);
        c.setStatus(status); c.setPlanId(planId); c.setAddress(address);
        c.setCity(city); c.setState(state); c.setMonthlyUsageMb(usageMb);
        c.setContractStart(LocalDateTime.of(2024, 1, 15, 0, 0));
        return c;
    }

    private double getPlanBase(PlanRepository repo, Long planId) {
        return planId != null ? repo.findById(planId).map(Plan::getBasePrice).orElse(89.90) : 89.90;
    }
}
