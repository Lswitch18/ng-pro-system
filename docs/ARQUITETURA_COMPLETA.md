# NG Pro ISP Billing System - Documentação Completa

## 1. Visão Geral do Sistema

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         NG PRO ISP BILLING SYSTEM                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                 │
│  │   FRONTEND   │    │   BACKEND    │    │  EXTERNAL    │                 │
│  │    (React)   │◄──►│  (Spring)    │◄──►│    API       │                 │
│  │   :3001      │    │   :4001      │    │   :4001      │                 │
│  └──────────────┘    └──────┬───────┘    └──────────────┘                 │
│                            │                                               │
│         ┌──────────────────┼──────────────────┐                          │
│         │                  │                  │                          │
│         ▼                  ▼                  ▼                          │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐                     │
│  │  FREERADIUS │   │  WhatsApp   │   │   Mailpit   │                     │
│  │   :1812     │   │    Bot      │   │   :8025     │                     │
│  └─────────────┘   └─────────────┘   └─────────────┘                     │
│         │                  │                  │                          │
│         ▼                  ▼                  ▼                          │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐                     │
│  │ NAS Sim     │   │  WhatsApp   │   │    SMTP     │                     │
│  │ (MikroTik)  │   │   Cloud     │   │   Server    │                     │
│  └─────────────┘   └─────────────┘   └─────────────┘                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 2. Arquitetura de Microsserviços

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ARQUITETURA DE SERVIÇOS                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │                    NG-PRO-BACKEND (Spring Boot)                    │    │
│  │  ┌─────────────────────────────────────────────────────────────┐  │    │
│  │  │                     CORE BUSINESS LAYER                       │  │    │
│  │  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐ │  │    │
│  │  │  │  Billing    │ │  Dunning    │ │    Provisioning        │ │  │    │
│  │  │  │  Engine     │ │  Service    │ │    Service             │ │  │    │
│  │  │  │             │ │  (Cobração)  │ │    (Ativa/Suspende)    │ │  │    │
│  │  │  └─────────────┘ └─────────────┘ └─────────────────────────┘ │  │    │
│  │  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐ │  │    │
│  │  │  │ Collection  │ │   Usage     │ │    Integration         │ │  │    │
│  │  │  │  Service    │ │  Processor  │ │    Hub                 │ │  │    │
│  │  │  │  (Pix/Bol)  │ │  (Dados)     │ │    (SAP/Radius)         │ │  │    │
│  │  │  └─────────────┘ └─────────────┘ └─────────────────────────┘ │  │    │
│  │  └─────────────────────────────────────────────────────────────┘  │    │
│  │                              │                                       │    │
│  │  ┌─────────────────────────────────────────────────────────────┐  │    │
│  │  │                     DATA LAYER (JPA)                        │  │    │
│  │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │  │    │
│  │  │  │Customer  │ │ Invoice  │ │   Plan   │ │ApiKey    │       │  │    │
│  │  │  │  Repo    │ │   Repo   │ │   Repo   │ │  Repo    │       │  │    │
│  │  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │  │    │
│  │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │  │    │
│  │  │  │AppUser   │ │ RadCheck │ │UsageEvent│ │ Lock     │       │  │    │
│  │  │  │  Repo    │ │   Repo   │ │   Repo   │ │ Manager  │       │  │    │
│  │  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │  │    │
│  │  └─────────────────────────────────────────────────────────────┘  │    │
│  │                              │                                       │    │
│  │  ┌─────────────────────────────────────────────────────────────┐  │    │
│  │  │                   SECURITY LAYER                             │  │    │
│  │  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐ │  │    │
│  │  │  │    JWT      │ │  Security   │ │    API Key              │ │  │    │
│  │  │  │   Filter    │ │   Config    │ │    Authentication       │ │  │    │
│  │  │  └─────────────┘ └─────────────┘ └─────────────────────────┘ │  │    │
│  │  └─────────────────────────────────────────────────────────────┘  │    │
│  └────────────────────────────────────────────────────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 3. Modelo de Dados (ER)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         MODELO DE DADOS                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌────────────────┐          ┌────────────────┐                          │
│   │   app_users   │          │     plans      │                          │
│   ├────────────────┤          ├────────────────┤                          │
│   │ id (PK)       │◄─────────│ id (PK)        │                          │
│   │ username      │          │ name           │                          │
│   │ password_hash │          │ description    │                          │
│   │ role          │          │ base_price     │                          │
│   │ created_at    │          │ speed           │                          │
│   └────────────────┘          │ data_cap_mb    │                          │
│                               │ family_plan     │                          │
│                               │ tier1_limit_mb  │                          │
│                               │ tier2_price_mb  │                          │
│                               └────────────────┘                          │
│                                     │                                      │
│                                     │ 1:N                                  │
│                                     ▼                                      │
│   ┌────────────────┐          ┌────────────────┐                          │
│   │   customers   │          │   invoices    │                          │
│   ├────────────────┤          ├────────────────┤                          │
│   │ id (PK)       │──────────►│ id (PK)       │                          │
│   │ name          │    N:1   │ customer_id(FK)│                         │
│   │ email         │          │ plan_id (FK)  │                          │
│   │ phone         │          │ status         │                          │
│   │ cpf_cnpj      │          │ base_amount    │                          │
│   │ status        │          │ overage_amount │                          │
│   │ plan_id (FK)  │          │ total_amount   │                          │
│   │ address       │          │ due_date        │                          │
│   │ city          │          │ paid_at         │                          │
│   │ state         │          │ reference_month │                         │
│   │ monthly_usage │          │ billing_process │                         │
│   │ contract_start│          │ created_at      │                          │
│   │ created_at    │          └────────────────┘                          │
│   └────────────────┘                                                     │
│                                     ▲                                      │
│                                     │                                      │
│   ┌────────────────┐              │                                      │
│   │   api_keys    │              │                                      │
│   ├────────────────┤              │                                      │
│   │ id (PK)       │◄─────────────┘                                      │
│   │ client_name   │                                                    │
│   │ api_key       │                                                    │
│   │ client_type   │                                                    │
│   │ permissions   │                                                    │
│   │ status        │                                                    │
│   │ rate_limit    │                                                    │
│   │ expires_at    │                                                    │
│   │ last_used     │                                                    │
│   │ created_at    │                                                    │
│   └────────────────┘                                                    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 4. Fluxo de Billing (Faturamento)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    FLUXO DE FATURAMENTO MENSAL                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────┐                                                          │
│   │  Agendado   │  Cron: 0 0 1 * * ? (Todo dia 1º às 00:00)                 │
│   │   (SCHEDULED)│                                                         │
│   └──────┬───────┘                                                          │
│          │                                                                   │
│          ▼                                                                   │
│   ┌──────────────────────────────────────┐                                  │
│   │ 1. Adquirir Lock Global              │                                  │
│   │    (LockManager - prevents concurrent│                                  │
│   │     billing runs in cluster)        │                                  │
│   └──────┬───────────────────────────────┘                                  │
│          │                                                                   │
│          ▼                                                                   │
│   ┌──────────────────────────────────────┐                                  │
│   │ 2. Para cada Cliente ATIVO:          │                                  │
│   │    ┌────────────────────────────┐    │                                  │
│   │    │ a) Buscar Plano do cliente │    │                                  │
│   │    │ b) Calcular base (R$89.90) │    │                                  │
│   │    │ c) Verificar uso mensal    │    │                                  │
│   │    │ d) Calcular overage (se    │    │                                  │
│   │    │    uso > tier1_limit)      │    │                                  │
│   │    │ e) Gerar Invoice           │    │                                  │
│   │    └────────────────────────────┘    │                                  │
│   └──────┬───────────────────────────────┘                                  │
│          │                                                                   │
│          ▼                                                                   │
│   ┌──────────────────────────────────────┐                                  │
│   │ 3. CollectionService.emitCollection │                                  │
│   │    - Gera código PIX                 │                                  │
│   │    - Envia notificação WhatsApp      │                                  │
│   └──────┬───────────────────────────────┘                                  │
│          │                                                                   │
│          ▼                                                                   │
│   ┌──────────────────────────────────────┐                                  │
│   │ 4. IntegrationHub.syncToSAP           │                                  │
│   │    - Sincroniza com ERP/SAP           │                                  │
│   └──────┬───────────────────────────────┘                                  │
│          │                                                                   │
│          ▼                                                                   │
│   ┌──────────────────────────────────────┐                                  │
│   │ 5. Liberar Lock                      │                                  │
│   │    + Log de conclusão                │                                  │
│   └──────────────────────────────────────┘                                  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 5. Fluxo de Dunning (Cobrança)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    FLUXO DE COBRANÇA (DUNNING)                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────┐                                                          │
│   │  Agendado   │  Cron: 0 0 * * * ? (A cada hora)                          │
│   │   (SCHEDULED)│                                                         │
│   └──────┬───────┘                                                          │
│          │                                                                   │
│          ▼                                                                   │
│   ┌──────────────────────────────────────┐                                  │
│   │ 1. Buscar faturas PENDING            │                                  │
│   └──────┬───────────────────────────────┘                                  │
│          │                                                                   │
│          ▼                                                                   │
│   ┌──────────────────────────────────────┐                                  │
│   │ 2. Para cada fatura:                 │                                  │
│   │    ┌────────────────────────────┐    │                                  │
│   │    │ SE vencida há +5 dias?    │    │                                  │
│   │    │   │                       │    │                                  │
│   │    ├─SIM───────────────────────▼──┐│                                  │
│   │    │ - Marcar como OVERDUE       ││                                  │
│   │    │ - ProvisioningService       ││                                  │
│   │    │   .suspendService()        ││                                  │
│   │    │ - Notificar cliente        ││                                  │
│   │    │   (WhatsApp/SMS/Email)     ││                                  │
│   │    └────────────────────────────┘│                                  │
│   │    │NÃO                         │    │                                  │
│   │    ├─SIM───────────────────────▼──┐│                                  │
│   │    │ - Marcar como OVERDUE       ││                                  │
│   │    └────────────────────────────┘│                                  │
│   │    │NÃO                          │    │                                  │
│   │    └──────────────────────────────┘                                  │
│   └──────┬───────────────────────────────┘                                  │
│          │                                                                   │
│          ▼                                                                   │
│   ┌──────────────────────────────────────┐                                  │
│   │ 3. Liberar Lock                     │                                  │
│   └──────────────────────────────────────┘                                  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 6. Fluxo de Provisioning (Ativação/Suspensão)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    FLUXO DE PROVISIONING                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                  PROVISIONING SERVICE                               │  │
│   ├─────────────────────────────────────────────────────────────────────┤  │
│   │                                                                     │  │
│   │  ┌────────────────┐    ┌────────────────┐    ┌────────────────┐    │  │
│   │  │  ACTIVATE     │    │   SUSPEND     │    │    BLOCK       │    │  │
│   │  │  (Novo cliente│    │  (Inadimplência│   │  (Fraude/      │    │  │
│   │  │   ou pagamento│    │   5+ dias)    │    │   Cancelamento)│   │  │
│   │  └───────┬────────┘    └───────┬────────┘    └───────┬────────┘    │  │
│   │          │                     │                     │              │  │
│   │          ▼                     ▼                     ▼              │  │
│   │  ┌────────────────────────────────────────────────────────────┐   │  │
│   │  │  IntegrationHub.authorizeRadiusAccess(customerId, true/false)│ │  │
│   │  │  - Envia CoA (Change of Authorization) ao FreeRADIUS      │   │  │
│   │  │  - Atualiza tabela radcheck no banco do RADIUS            │   │  │
│   │  └────────────────────────────────────────────────────────────┘   │  │
│   │          │                     │                     │              │  │
│   │          ▼                     ▼                     ▼              │  │
│   │  ┌────────────────────────────────────────────────────────────┐   │  │
│   │  │  IntegrationHub.notifyCustomer()                          │   │  │
│   │  │  - WhatsApp: "Bem-vindo ao NG Pro!"                       │   │  │
│   │  │  - SMS: "Serviço ativado"                                 │   │  │
│   │  │  - Email: "Confirmación de ativação"                     │   │  │
│   │  └────────────────────────────────────────────────────────────┘   │  │
│   │          │                     │                     │              │  │
│   │          ▼                     ▼                     ▼              │  │
│   │  ┌────────────────────────────────────────────────────────────┐   │  │
│   │  │  CustomerRepository.save()                                 │   │  │
│   │  │  - Status: ACTIVE / SUSPENDED / BLOCKED                     │   │  │
│   │  └────────────────────────────────────────────────────────────┘   │  │
│   │                                                                     │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 7. Autenticação e Segurança

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ARQUITETURA DE SEGURANÇA                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                    SECURITY CONFIG                                  │  │
│   ├─────────────────────────────────────────────────────────────────────┤  │
│   │                                                                     │  │
│   │  ┌─────────────┐                                                   │  │
│   │  │  JWT Filter │                                                   │  │
│   │  │             │                                                   │  │
│   │  │ 1. Extrai  │                                                   │  │
│   │  │    token   │                                                   │  │
│   │  │ 2. Valida  │                                                   │  │
│   │  │    JWT     │                                                   │  │
│   │  │ 3. Extrai  │                                                   │  │
│   │  │    user    │                                                   │  │
│   │  │ 4. Config  │                                                   │  │
│   │  │    Auth    │                                                   │  │
│   │  └──────┬──────┘                                                   │  │
│   │         │                                                          │  │
│   │         ▼                                                          │  │
│   │  ┌────────────────────────────────────────────────────────────┐  │  │
│   │  │                   ENDPOINTS PÚBLICOS                         │  │  │
│   │  │  /api/auth/*      - Login, register                         │  │  │
│   │  │  /api/test/*      - Test endpoints                           │  │  │
│   │  │  /api/whatsapp/*  - Webhook WhatsApp                         │  │  │
│   │  │  /api/radius/*    - RADIUS endpoints                          │  │  │
│   │  │  /api/nas/*       - NAS/MikroTik endpoints                    │  │  │
│   │  │  /api/v1/external/* - External API (API Key auth)             │  │  │
│   │  │  /swagger-ui/*    - API documentation                        │  │  │
│   │  └────────────────────────────────────────────────────────────┘  │  │
│   │                                                                     │  │
│   │         │                                                          │  │
│   │         ▼                                                          │  │
│   │  ┌────────────────────────────────────────────────────────────┐  │  │
│   │  │                   ENDPOINTS PROTEGIDOS                     │  │  │
│   │  │  /api/customers/*  - CRUD Clientes        (JWT Required)   │  │  │
│   │  │  /api/invoices/*  - Faturas e Billing    (JWT Required)   │  │  │
│   │  │  /api/plans/*      - Planos               (JWT Required)   │  │  │
│   │  │  /api/dashboard/* - Dashboard             (JWT Required) │  │  │
│   │  └────────────────────────────────────────────────────────────┘  │  │
│   │                                                                     │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                    API KEY AUTH (External API)                     │  │
│   ├─────────────────────────────────────────────────────────────────────┤  │
│   │                                                                     │  │
│   │  Header: X-API-Key: <chave>                                        │  │
│   │                                                                     │  │
│   │  ┌────────────────────────────────────────────────────────────┐    │  │
│   │  │  ApiKeyService.validateApiKey()                          │    │  │
│   │  │    1. Busca chave no banco                                 │    │  │
│   │  │    2. Verifica status (ACTIVE)                            │    │  │
│   │  │    3. Verifica expiração                                  │    │  │
│   │  │    4. Verifica permissões                                 │    │  │
│   │  │    5. Atualiza last_used                                  │    │  │
│   │  └────────────────────────────────────────────────────────────┘    │  │
│   │                                                                     │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 8. Fluxo de Usage (Consumo de Dados)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PROCESSAMENTO DE USO (DATA)                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                     NAS SIMULATOR (MikroTik)                        │  │
│   │  - Simula Access-Request do cliente                                 │  │
│   │  - Envia dados de consumo para FreeRADIUS                          │  │
│   └──────────────────────────────┬──────────────────────────────────────┘  │
│                                  │                                          │
│                                  ▼                                          │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                     FREERADIUS                                      │  │
│   │  - Recebe Accounting-Request                                        │  │
│   │  - Registra na tabela radacct                                       │  │
│   │  - Forward para backend (opcional)                                  │  │
│   └──────────────────────────────┬──────────────────────────────────────┘  │
│                                  │                                          │
│                                  ▼                                          │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                UsageProcessor (Async Event)                         │  │
│   │  ┌────────────────────────────────────────────────────────────┐    │  │
│   │  │ @Async + @EventListener(UsageEvent)                       │    │  │
│   │  │ 1. Recebe evento de uso                                    │    │  │
│   │  │ 2. Processa (agrega, transforma)                           │    │  │
│   │  │ 3. Atualiza Customer.monthly_usage_mb                    │    │  │
│   │  │ 4. Log para debug                                           │    │  │
│   │  └────────────────────────────────────────────────────────────┘    │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 9. Stack Tecnológico

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         STACK TECNOLÓGICO                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  FRONTEND                                                                  │
│  ├── React 18 + TypeScript                                                 │
│  ├── Vite (build tool)                                                     │
│  ├── TailwindCSS (styling)                                                │
│  └── Axios (HTTP client)                                                  │
│                                                                             │
│  BACKEND                                                                   │
│  ├── Java 17                                                               │
│  ├── Spring Boot 3.2.0                                                    │
│  │   ├── Spring Security (JWT)                                           │
│  │   ├── Spring Data JPA                                                 │
│  │   ├── Spring Mail                                                     │
│  │   └── Springdoc OpenAPI (Swagger)                                    │
│  ├── Hibernate 6.4                                                        │
│  ├── SQLite (banco de dados)                                              │
│  ├── JWT (autenticação)                                                   │
│  └── Lombok (减少 boilerplate)                                           │
│                                                                             │
│  INFRASTRUCTURE                                                            │
│  ├── Docker + Docker Compose                                             │
│  ├── FreeRADIUS (AAA Server)                                              │
│  ├── NAS Simulator (MikroTik sim)                                         │
│  ├── WhatsApp Bot (Venom/Baileys)                                         │
│  └── Mailpit ( SMTP test server)                                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 10. Endpoints da API

### API Interna (JWT Required)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/customers` | Listar clientes |
| GET | `/api/customers/{id}` | Buscar cliente |
| POST | `/api/customers` | Criar cliente |
| PUT | `/api/customers/{id}` | Atualizar cliente |
| DELETE | `/api/customers/{id}` | Deletar cliente |
| PATCH | `/api/customers/{id}/status` | Atualizar status |
| GET | `/api/invoices` | Listar faturas |
| GET | `/api/invoices/customer/{id}` | Faturas por cliente |
| GET | `/api/invoices/status/{status}` | Faturas por status |
| POST | `/api/invoices/{id}/pay` | Registrar pagamento |
| POST | `/api/invoices/run-billing` | Executar faturamento |
| POST | `/api/invoices/run-dunning` | Executar cobrança |
| GET | `/api/invoices/overdue-customers` | Clientes inadimplentes |
| GET | `/api/plans` | Listar planos |
| GET | `/api/dashboard/stats` | Estatísticas |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/register` | Registrar |

### API Externa (API Key Required)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/v1/external/health` | Health check |
| GET | `/api/v1/external/customers` | Listar clientes |
| GET | `/api/v1/external/customers/{id}` | Buscar cliente |
| POST | `/api/v1/external/customers` | Criar cliente |
| PUT | `/api/v1/external/customers/{id}` | Atualizar cliente |
| GET | `/api/v1/external/invoices` | Listar faturas |
| GET | `/api/v1/external/invoices/{id}` | Buscar fatura |
| POST | `/api/v1/external/invoices/{id}/pay` | Pagar fatura |
| GET | `/api/v1/external/plans` | Listar planos |
| POST | `/api/v1/external/auth/create-key` | Criar API Key |

## 11. Configurações e Variáveis de Ambiente

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    VARIÁVEIS DE AMBIENTE                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  SPRING_PROFILES_ACTIVE=prod                                               │
│  SERVER_PORT=4001                                                           │
│  JWT_SECRET=ngproenterprisebillingsecretkey2024distlockcdr                │
│                                                                             │
│  # FreeRADIUS                                                               │
│  RADSECRET=testing123                                                      │
│                                                                             │
│  # WhatsApp Bot                                                            │
│  NODE_ENV=production                                                       │
│                                                                             │
│  # Mailpit                                                                  │
│  MAIL_HOST=mailpit                                                         │
│  MAIL_PORT=1025                                                            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 12. Casos de Uso Principais

### 12.1 - Novo Cliente
```
1. Operador cria cliente via Frontend (POST /api/customers)
2. EmailService envia email de boas-vindas
3. ProvisioningService.activateService() é chamado
4. IntegrationHub.authorizeRadiusAccess(true)
5. Cliente pode acessar a internet
```

### 12.2 - Faturamento Mensal
```
1. BillingEngine.runBillingProcess() roda no cron
2. Para cada cliente ACTIVE:
   - Calcula base + overage
   - Cria Invoice
   - CollectionService.emitCollection() → PIX code
   - IntegrationHub.notifyCustomer() → WhatsApp
3. Clientes recebem notificação com link de pagamento
```

### 12.3 - Cobrança (Inadimplência)
```
1. DunningService.runDunningProcess() roda a cada hora
2. Para cada fatura PENDING vencida:
   - SE +5 dias: SUSPENDE serviço + marca OVERDUE
   - SE vencida: marca OVERDUE
3. ProvisioningService.suspendService() → bloqueia acesso
4. Cliente recebe notificação
```

### 12.4 - Pagamento
```
1. Cliente paga via gateway externo (MercadoPago, etc)
2. Sistema recebe webhook de confirmação
3. CollectionService.processConciliation() → marca PAID
4. ProvisioningService.activateService() → restabelece acesso
5. EmailService.sendReceipt() → envia recibo
```

### 12.5 - Integração Externa (Prestashop)
```
1. Sistema externo obtém API Key
2. Faz requisição com X-API-Key header
3. ApiKeyService valida chave + permissões
4. ExternalApiController processa requisição
5. Retorna dados em JSON
```

## 13. Escalabilidade e Locks

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    GERENCIAMENTO DE LOCKS (CLUSTER)                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  LockManager usa ConcurrentHashMap + ReentrantLock                        │
│                                                                             │
│  Recursos protegidos:                                                       │
│  ├── GLOBAL_BILLING_RUN - Apenas 1 processo de faturamento por vez       │
│  ├── GLOBAL_DUNNING_RUN - Apenas 1 processo de cobrança por vez          │
│  └── (podem ser adicionados mais conforme necessidade)                   │
│                                                                             │
│  BENEFÍCIOS:                                                               │
│  • Prevente billing duplicado em ambiente clusterizado                    │
│  • Permite múltiplas réplicas do backend                                  │
│  • Sem necessidade de Redis/zookeeper para locks simples                  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 14. Permissões de API Key

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PERMISSÕES DA API EXTERNA                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Permissão       │ Descrição                                               │
│  ─────────────────────────────────────────────────────────────            │
│  customers       │ CRUD completo de clientes                               │
│  invoices        │ Consulta de faturas                                     │
│  plans           │ Consulta de planos                                      │
│  collections     │ Processar cobranças/pagamentos                          │
│  apikeys         │ Criar novas API Keys                                    │
│                                                                             │
│  EXEMPLO:                                                                  │
│  permissions: "customers,invoices,plans"                                   │
│                                                                             │
│  TAXA LIMITADA:                                                            │
│  • Padrão: 100 requisições/hora                                           │
│  • Configurável por chave                                                  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 15. Docker Compose - Serviços

```yaml
ng-pro-backend:      # Spring Boot API (porta 4001)
ng-pro-frontend:     # React App (porta 3001)
mailpit:             # Servidor SMTP (portas 1025, 8025)
whatsapp-bot:       # Bot WhatsApp (porta 4002)
freeradius:          # RADIUS Server (portas 1812, 1813 UDP)
nas-simulator:      # Simulador NAS/MikroTik (porta 4003)
```

## 16. Próximos Passos / Melhorias

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    MELHORIAS SUGERIDAS                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ALTA PRIORIDADE                                                          │
│  ├── Integrar gateway de pagamento real (MercadoPago/PJBank)            │
│  ├── Webhook para conciliação automática de pagamentos                    │
│  ├── Relatórios financeiros avançados                                     │
│  └── Sistema de login via OAuth2 (Google, Facebook)                       │
│                                                                             │
│  MÉDIA PRIORIDADE                                                         │
│  ├── Substituir SQLite por PostgreSQL (produção)                         │
│  ├── Cache com Redis                                                       │
│  ├── Rate limiting mais robusto                                           │
│  └── Métricas com Prometheus/Grafana                                       │
│                                                                             │
│  BAIXA PRIORIDADE                                                         │
│  ├── App mobile (React Native/Flutter)                                    │
│  ├── Chatbot com IA para suporte                                          │
│  ├── Portal do cliente (self-service)                                     │
│  └── Integração contabilidade (NF-e)                                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

**Versão:** 1.0.0  
**Última Atualização:** 2026-03-27  
**Sistema:** NG Pro ISP Billing  
**Stack:** Java 17 + Spring Boot 3.2 + React + Docker
