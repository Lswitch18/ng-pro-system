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

### Passo 4: Build Triggers

1. Marque **"Poll SCM"**
2. No campo **Schedule:**
   ```
   H/5 * * * *
   ```
   > Isso verifica mudanças no GitHub a cada 5 minutos

3. (Opcional) Marque **"GitHub hook trigger for GITScm polling"** para trigger automático

---

### Passo 5: Build (Configurar Build)

1. Clique em **"Adicionar passo de build"** → **"Executar shell"**
2. No campo de comando, cole:

```bash
#!/bin/bash

echo "=========================================="
echo "NG-PRO System - Build Pipeline"
echo "=========================================="

# Build Backend
echo "[1/4] Compilando Backend Java..."
cd backend
mvn clean compile -DskipTests
cd ..

# Build Frontend  
echo "[2/4] Build Frontend Node.js..."
cd frontend
npm install
npm run build
cd ..

# Build Docker Images
echo "[3/4] Build Docker Images..."
docker build -t ngpro-backend:latest ./backend
docker build -t ngpro-frontend:latest ./frontend

echo "[4/4] Build concluído com sucesso!"
```

---

### Passo 6: Configurar Post-Build (Opcional)

Para notificação por email:

1. Clique em **"Adicionar ação de pós-build"** → **"E-mail Notification"**
2. **Recipients:** `admin@lswitch.com`
3. Marque **"Send e-mail for every unstable build"**

---

### Passo 7: Salvar

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
