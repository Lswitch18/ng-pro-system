# Opções de CI/CD para NG-PRO System

## Resumo das Opções

| Opção | Ferramentas | Dificuldade | Recomendado |
|-------|-------------|-------------|-------------|
| **GitHub Actions** | Maven, Docker, Node.js | ✅ Fácil | 🔥 **SIM** |
| Jenkins Local | Precisa instalar ferramentas | Difícil | ❌ Não |

---

## Opção 1: GitHub Actions (RECOMENDADO) ✅

O pipeline já está configurado e funcionando automaticamente.

### Como usar:
1. Acesse: https://github.com/Lswitch18/ng-pro-system/actions
2. Clique em **"CI/CD Pipeline"**
3. O build executa automaticamente a cada push

### O que faz:
- ✅ Compila Java com Maven
- ✅ Executa testes
- ✅ Análise de segurança (OWASP)
- ✅ Build Docker
- ✅ Deploy (se configurado)

### Configurar secrets (para Docker push):
1. Repo GitHub → Settings → Secrets → New repository secret
2. Adicione:
   - `DOCKER_USERNAME` → seu usuário Docker Hub
   - `DOCKER_PASSWORD` → sua senha Docker Hub

---

## Opção 2: Jenkins com Build Local

### Requisitos:
O Jenkins precisa ter instalado:
- Maven (`mvn`)
- Node.js + npm
- Docker

### Instalação no Jenkins:

```bash
# Acesse o container Jenkins
docker exec -it portfolio-audit-defesa-jenkins-1 bash

# Instale Maven
apt-get update
apt-get install -y maven

# Instale Node.js
apt-get install -y nodejs npm

# Instale Docker
apt-get install -y docker.io

# Saia do container
exit
```

### Monte o Docker socket:
```bash
# Pare o Jenkins
docker stop portfolio-audit-defesa-jenkins-1

# Remova e recrie com Docker socket
docker rm portfolio-audit-defesa-jenkins-1

docker run -d \
  --name portfolio-audit-defesa-jenkins-1 \
  -p 8080:8080 \
  -p 50000:50000 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts-jdk17
```

---

## Conclusão

**Recomendação:** Use o **GitHub Actions** que já está configurado!

1. Push no código → Actions executa
2. Ver resultados em: https://github.com/Lswitch18/ng-pro-system/actions
3. Não precisa de servidor próprio

---

## Próximos Passos

1. ✅ Verificar GitHub Actions: https://github.com/Lswitch18/ng-pro-system/actions
2. ⏳ Configurar secrets (Docker Hub) se quiser push automático
3. ⏳ Configurar deploy se tiver servidor

---

## Comandos Úteis

### Ver GitHub Actions
```bash
# Não precisa, é via web
# Acesse: https://github.com/Lswitch18/ng-pro-system/actions
```

### Ver logs Jenkins
```bash
docker logs portfolio-audit-defesa-jenkins-1
```

### Ver jobs Jenkins
```bash
curl http://localhost:8080/api/json
```
