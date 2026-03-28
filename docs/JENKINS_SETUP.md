# Guia para Criar Job no Jenkins - NG-PRO System

## Acessar o Jenkins

1. Abra o navegador e akses:
   ```
   http://localhost:8080
   ```

---

## Criar Novo Job

### Passo 1: Novo Item
1. Clique em **"Novo Item"** (menu lateral esquerdo)
2. Nome do item: `ng-pro-system`
3. Selecione: **"Freestyle project"**
4. Clique em **OK**

---

### Passo 2: Configuração Geral

Na página de configuração do job:

1. **Descrição** (opcional):
   ```
   NG-PRO Enterprise Billing System - Pipeline de CI/CD
   ```

---

### Passo 3: Configuração do Git (Código Fonte)

1. Marque a opção **Git**
2. **Repository URL:**
   ```
   https://github.com/Lswitch18/ng-pro-system.git
   ```
3. **Branch Specifier (blank for 'any'):**
   ```
   */main
   ```

---

### Passo 4: Build Triggers (Gatilhos de Build)

Marque as opções desejadas:

#### ✅ GitHub hook trigger for GITScm polling
- **Recomendado para CI/CD automático**
- Disparado automaticamente quando há push no GitHub

#### ✅ Consultar periodicamente o SCM (Poll SCM)
- Verifica mudanças no repositório periodicamente
- No campo **Schedule:**
  ```
  H/5 * * * *
  ```
  > Verifica a cada 5 minutos

#### ✅ Construir periodicamente
- Executa o build em horários agendados
- Exemplo para executar diariamente às 9h:
  ```
  H 9 * * 1-5
  ```

#### ✅ Construir após a construção de outros projetos
- Executa este job após outro job finalizar
- Útil para pipelines dependentes

---

**Configuração recomendada:**
Marque apenas **"GitHub hook trigger for GITScm polling"** para automático instantâneo.

---

### Passo 5: Environment (Ambiente)

Opcional - Configurações de ambiente:

#### ✅ Delete workspace before build starts
- Limpa o diretório de trabalho antes de cada build
- Recomendado para evitar problemas de cache

#### ✅ Add timestamps to the Console Output
- Adiciona timestamps aos logs do console
- Útil para debugging

---

### Passo 6: Build (Configurar Build)

1. Clique em **"Adicionar passo de build"** → **"Executar shell"**
2. No campo de comando, cole:

```bash
#!/bin/bash

echo "=========================================="
echo "NG-PRO System - Build Pipeline"
echo "=========================================="

# Verificar código clonado
echo "[INFO] Diretório de trabalho: $(pwd)"
echo "[INFO] Listando arquivos..."
ls -la

# Verificar se o código foi clonado corretamente
if [ -d "backend" ]; then
    echo "[OK] Backend encontrado"
    ls -la backend/
else
    echo "[ERRO] Backend não encontrado!"
    exit 1
fi

if [ -d "frontend" ]; then
    echo "[OK] Frontend encontrado"
    ls -la frontend/
else
    echo "[ERRO] Frontend não encontrado!"
    exit 1
fi

echo "=========================================="
echo "Build verificado com sucesso!"
echo "=========================================="
echo ""
echo "NOTA: Para executar build completo (Maven/Docker),"
echo "instale as ferramentas no Jenkins ou use CI/CD do GitHub."
```

---

### Passo 7: Ações de Pós-Build

Clique em **"Adicionar ação de pós-build"** e escolha:

#### ✅ E-mail Notification
- Envia email após o build
- **Recipients:** `admin@lswitch.com`
- Marque **"Send e-mail for every unstable build"**

#### ✅ Archive the artifacts
- Salva arquivos gerados pelo build
- Útil para guardar JARs, WARs, etc.

#### ✅ Build other projects
- Dispara outro job após este finalizar

---

### Passo 8: Salvar

1. Clique no botão **"Salvar"** (no final da página)

---

## Executar o Build

### Opção 1: Manual
1. Na página do job, clique em **"Construir agora"** (menu lateral)

### Opção 2: Automático (via Webhook)
1. O Jenkins verificará automaticamente a cada 5 minutos
2. Para trigger instantâneo, configure o Webhook no GitHub

---

## Configurar Webhook no GitHub (Opcional)

1. Acesse: https://github.com/Lswitch18/ng-pro-system/settings/hooks
2. Clique em **"Add webhook"**
3. **Payload URL:**
   ```
   http://SEU_IP:8080/github-webhook/
   ```
4. **Content type:** `application/json`
5. **Events:** Selecione **"Just the push event"**
6. Clique em **"Add webhook"**

---

## Monitorar Build

1. Na página do job, você verá:
   - **Console Output** - logs do build
   - **Status do Build** - azul (sucesso) / vermelho (falha)

---

## Solução de Problemas

### Jenkins não inicia
```bash
docker restart portfolio-audit-defesa-jenkins-1
```

### Verificar logs
```bash
docker logs portfolio-audit-defesa-jenkins-1
```

---

## Arquivo Jenkinsfile (Alternativa Pipeline)

Se preferir usar **Pipeline** (em vez de Freestyle):

1. Novo Item → selecione **Pipeline**
2. Em **Pipeline** → selecione **"Pipeline script from SCM"**
3. SCM: **Git**
4. Repository URL: `https://github.com/Lswitch18/ng-pro-system.git`
5. Script Path: `Jenkinsfile`

O Jenkinsfile já está configurado no repositório.

---

## Contato

Em caso de dúvidas, verifique os logs em:
- Jenkins: Console Output do build
- Docker: `docker logs portfolio-audit-defesa-jenkins-1`
