# billing-system-ISP

<p align="center">
  <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white" alt="Angular">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
  <img src="https://img.shields.io/badge/Nginx-269539?style=for-the-badge&logo=nginx&logoColor=white" alt="Nginx">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/status-Desenvolvimento-blue?style=for-the-badge" alt="Status">
  <img src="https://img.shields.io/github/license/Lswitch18/billing-system-ISP?style=for-the-badge" alt="License">
  <img src="https://img.shields.io/github/last-commit/Lswitch18/billing-system-ISP/main?style=for-the-badge" alt="Last Commit">
</p>

---

## 請求 システム  billing-system-ISP

**Sistema completo de gestão de billing e autenticação para provedores de Internet (ISP)**

O billing-system-ISP é uma solução enterprise completa para gestão de provedores de internet, combinando múltiplas tecnologias modernas para fornecer uma plataforma robusta de billing, gestão de clientes, radius authentication, e automação via WhatsApp.

---

## Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    billing-system-ISP                    │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │   FRONTEND      │  │   BACKEND   │  │  WHATSAPP    │  │
│  │   (Angular)     │◄─┤  (Spring)   │  │    BOT       │  │
│  │   :4200        │  │   :8080    │  │  (Node.js)   │  │
│  └───────┬─────────┘  └──────┬─────┘  └──────┬──────┘  │
│          │                  │               │           │          │
│          │           ┌─────▼─────┐        │           │          │
│          │           │ DATABASE  │        │           │          │
│          │           │ (Postgres) │        │           │          │
│          │           └───────────┘        │           │          │
│          │                  │              │           │          │
│          │           ┌─────▼─────────────┴───────┐    │          │
│          │           │       RADIUS              │    │          │
│          │           │    (FreeRADIUS)           │    │          │
│          │           │       :1812               │    │          │
│          │           └─────────────────────────┘    │          │
│          │                                             │        │
└──────────┼─────────────────────────────────────────────┘        │
           │                                                      │
           ▼                                                      │
      ┌─────────────────────────────────────────┐                  │
      │           NGINX REVERSE PROXY            │                  │
      │              :80 / :443                 │                  │
      └─────────────────────────────────────────┘                  │
                                                              │
      Internet ──────────────────────────────────────────────────┘
```

---

## Funcionalidades Principais

### Gestão de Billing
- Cadastro e gestão de clientes
- Planos de acesso personalizados
- Faturamento automático
- Controle de conexão por tempo/dados
- Geração de boletos e invoices
- Histórico completo de transações

### Autenticação Radius
- Protocolo RADIUS completo
- Autenticação PPPoE
- Hotspot Login
- Contabilidade de uso
- Multiple NAS support
- CoA (Change of Authorization)

### Portal do Cliente
- Login consciente
- Visualização de consumo
- 2ª via de boletos
- Abertura de tickets
- Alteração de planos

### WhatsApp Bot
- Consulta de saldo
- Envio de alertas
- Suporte a clientes
- Pagamentos confirmados
- Notificações automáticas

---

## Stack de Tecnologias

### Backend
| Tecnologia | Descrição |
|------------|-----------|
| Java 17 | Linguagem principal |
| Spring Boot 3.x | Framework web |
| Spring Security 6.x | Autenticação |
| PostgreSQL 15+ | Banco de dados |
| Hibernate 6.x | ORM |

### Frontend
| Tecnologia | Descrição |
|------------|-----------|
| Angular 17+ | Framework SPA |
| TypeScript 5.x | Linguagem |
| TailwindCSS 3.x | Estilização |
| Vite 5.x | Bundler |
| Nginx | Servidor web |

### Infraestrutura
| Tecnologia | Descrição |
|------------|-----------|
| Docker | Containerização |
| Docker Compose | Orquestração |
| FreeRADIUS | Servidor RADIUS |
| PostgreSQL | Banco de dados |

---

## Começando

### Pré-requisitos
- Docker
- Docker Compose
- Git

### Instalação Rápida

```bash
# Clone o repositório
git clone https://github.com/Lswitch18/billing-system-ISP.git
cd billing-system-ISP

# Inicie todos os serviços
docker-compose up -d

# Acesse o frontend
# http://localhost:4200

# API disponível em
# http://localhost:8080
```

### Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| DB_HOST | Host PostgreSQL | localhost |
| DB_PORT | Porta PostgreSQL | 5432 |
| DB_NAME | Nome do banco | billing |
| DB_USER | Usuário DB | billing_user |
| DB_PASS | Senha DB | change_me |
| RADIUS_SECRET | Secret RADIUS | radius_secret |
| JWT_SECRET | Chave JWT | change_me_secure |
| WHATSAPP_SESSION | Session WhatsApp | - |

---

## Estrutura de Diretórios

```
billing-system-ISP/
├── backend/               # API REST (Spring Boot)
│   ├── src/
│   │   ├── main/java/
│   │   │   └── com/isp/billing/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       └── security/
│   │   └── resources/
│   │       └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/              # Portal do Cliente (Angular)
│   ├── src/
│   │   ├── app/
│   │   │   ├── pages/
│   │   │   ├── components/
│   │   │   └── services/
│   │   └── environments/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── radius/               # Configuração FreeRADIUS
│   └── docker-entrypoint-init.d/
│
├── whatsapp-bot/         # Bot WhatsApp (Node.js)
│   └── src/
│
├── docs/                 # Documentação
│   └── api/
│
├── docker-compose.yml    # Orquestração
├── Jenkinsfile          # Pipeline CI/CD
└── README.md
```

---

## API Endpoints

### Autenticação
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/auth/login | Login |
| POST | /api/auth/register | Registro |
| GET | /api/auth/me | Dados usuário |

### Clientes
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/clients | Listar clientes |
| POST | /api/clients | Criar cliente |
| GET | /api/clients/{id} | Detalhes cliente |
| PUT | /api/clients/{id} | Atualizar cliente |
| DELETE | /api/clients/{id} | Remover cliente |

### Planos
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/plans | Listar planos |
| POST | /api/plans | Criar plano |
| PUT | /api/plans/{id} | Atualizar plano |

### Faturamento
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/invoices | Listar faturas |
| POST | /api/invoices | Gerar fatura |
| GET | /api/invoices/{id} | Detalhes fatura |

### Radius
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/radius/auth | Autenticação RADIUS |
| POST | /api/radius/acct | Contabilidade RADIUS |
| POST | /api/radius/coa | Change of Auth |

---

## Desenvolvimento

### Rodando Localmente

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend (outro terminal)
cd frontend
npm install
npm start

# WhatsApp Bot (outro terminal)
cd whatsapp-bot
npm install
npm start
```

### Build de Produção

```bash
# Build todas as imagens
docker-compose build

# Subir em modo produção
docker-compose -f docker-compose.yml up -d
```

---

## Monitoramento

### Health Checks
```bash
# Backend
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost/

# PostgreSQL
docker exec billing-system-isp-postgres pg_isready
```

---

## Segurança

- JWT para autenticação API
- Bcrypt para hash de senhas
- Rate limiting no API Gateway
- RADIUS com secret compartilhado
- Conexões SSL/TLS
- Headers de segurança (CORS, CSP)

---

## Licença

MIT License - Copyright (c) 2024

---

<p align="center">
  <img src="https://komarev.com/ghpvc/?username=Lswitch18&repo=billing-system-ISP&style=flat-square&color=green" alt="Profile Views">
  <img src="https://img.shields.io/github/forks/Lswitch18/billing-system-ISP?style=flat-square" alt="Forks">
  <img src="https://img.shields.io/github/stars/Lswitch18/billing-system-ISP?style=flat-square" alt="Stars">
</p>

<p align="center">
  <sub>Feito com ☕ edeterminação</sub>
  <br>
  <a href="https://github.com/Lswitch18">
    <img src="https://img.shields.io/badge/-lswitch18-black?style=flat&logo=github" alt="lswitch18">
  </a>
</p>