# RADIUS Integration Guide

## Overview

The NG-PRO system integrates with FreeRADIUS for PPPoE and WiFi authentication.

## Architecture

```
[NAS/Router] ---> [FreeRADIUS] ---> [NG-PRO Backend]
    (Mikrotik,               (Auth/Acct)       (User validation)
     Ubiquiti, etc.)
```

## Configuration

### 1. Network Equipment (Mikrotik Example)

```routeros
# RADIUS Client Configuration
/radius add address=10.0.0.10 secret=ngpro_secret_key service=ppp,hotspot

# Enable RADIUS for PPP
/ppp aaa set use-radius=yes

# Hotspot RADIUS
/hotspot profile set hsprof1 use-radius=yes
```

### 2. Docker Services

Start RADIUS:
```bash
cd /home/lswitch/sec/Devsecops/ng-pro-system/radius
docker-compose up -d
```

## API Endpoints

### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/radius/users` | List all RADIUS users |
| GET | `/api/radius/users/{username}` | Get user details |
| POST | `/api/radius/users` | Create new user |
| PUT | `/api/radius/users/{username}` | Update password |
| DELETE | `/api/radius/users/{username}` | Delete user |
| POST | `/api/radius/users/sync` | Sync from billing |

### Example: Create User

```bash
curl -X POST http://localhost:4001/api/radius/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"username": "cliente1", "password": "senha123"}'
```

### Example: Sync from Billing

```bash
curl -X POST http://localhost:4001/api/radius/users/sync \
  -H "Authorization: Bearer TOKEN"
```

## Testing

### Test with radtest

```bash
# From inside the container
docker exec -it ng-pro-radius radtest testuser testpass localhost 1812 testing123
```

### Test from host

```bash
apt-get install -y freeradius-utils
radtest testuser testpass localhost 1812 testing123
```

## Port Configuration

| Port | Protocol | Purpose |
|------|----------|---------|
| 1812 | UDP | Authentication |
| 1813 | UDP | Accounting |

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| Framed-IP-Address | IP | Assigned IP address |
| Framed-Netmask | IP | Subnet mask |
| Session-Timeout | Integer | Max session time (seconds) |
| Acct-Interim-Interval | Integer | Accounting interval |
