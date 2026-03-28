# NG Pro External API - Documentação

API REST para integração com sistemas externos (Prestashop, ERPs, Apps móveis, etc).

## Autenticação

Todas as requisições devem incluir o header `X-API-Key`:

```
X-API-Key: ngpro_prestashop_test_key_2026
```

## Endpoints Disponíveis

### Health Check
```
GET /api/v1/external/health
```

### Gerenciar API Keys
```
POST /api/v1/external/auth/create-key
Header: X-API-Key: <sua-key>
Body: {
    "clientName": "Meu Cliente",
    "clientType": "PRESTASHOP",
    "permissions": "customers,invoices,plans"
}
```

### Clientes

**Listar clientes:**
```
GET /api/v1/external/customers
GET /api/v1/external/customers?status=ACTIVE&page=0&limit=50
```

**Buscar cliente por ID:**
```
GET /api/v1/external/customers/{id}
```

**Criar cliente:**
```
POST /api/v1/external/customers
{
    "name": "João Silva",
    "email": "joao@email.com",
    "phone": "(41) 99999-1111",
    "cpfCnpj": "123.456.789-00",
    "address": "Rua das Flores, 123",
    "city": "Curitiba",
    "state": "PR",
    "planId": 1
}
```

**Atualizar cliente:**
```
PUT /api/v1/external/customers/{id}
{
    "name": "Novo Nome",
    "status": "SUSPENDED",
    "planId": 2
}
```

### Faturas

**Listar faturas:**
```
GET /api/v1/external/invoices
GET /api/v1/external/invoices?customerId=1&status=PENDING&referenceMonth=2026-03
```

**Buscar fatura por ID:**
```
GET /api/v1/external/invoices/{id}
```

**Registrar pagamento:**
```
POST /api/v1/external/invoices/{id}/pay
```

### Planos

**Listar planos:**
```
GET /api/v1/external/plans
```

**Buscar plano por ID:**
```
GET /api/v1/external/plans/{id}
```

## Exemplo de Uso com cURL

```bash
# Health Check
curl -X GET http://localhost:4001/api/v1/external/health

# Listar clientes
curl -X GET http://localhost:4001/api/v1/external/customers \
  -H "X-API-Key: ngpro_prestashop_test_key_2026"

# Criar cliente
curl -X POST http://localhost:4001/api/v1/external/customers \
  -H "X-API-Key: ngpro_prestashop_test_key_2026" \
  -H "Content-Type: application/json" \
  -d '{"name":"Novo Cliente","email":"cliente@email.com","phone":"(41) 99999-9999","cpfCnpj":"999.999.999-99"}'

# Listar faturas de um cliente
curl -X GET "http://localhost:4001/api/v1/external/invoices?customerId=1" \
  -H "X-API-Key: ngpro_prestashop_test_key_2026"

# Pagar fatura
curl -X POST http://localhost:4001/api/v1/external/invoices/1/pay \
  -H "X-API-Key: ngpro_prestashop_test_key_2026"
```

## Códigos de Resposta

- `200 OK` - Sucesso
- `201 Created` - Criado com sucesso
- `400 Bad Request` - Dados inválidos
- `401 Unauthorized` - API Key inválida ou não fornecida
- `403 Forbidden` - Permissão insuficiente
- `404 Not Found` - Recurso não encontrado

## Permissões Disponíveis

- `customers` - Gerenciar clientes
- `invoices` - Consultar faturas
- `plans` - Consultar planos
- `collections` - Processar cobranças/pagamentos
- `apikeys` - Criar novas API Keys
