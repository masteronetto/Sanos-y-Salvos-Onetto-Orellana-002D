# Sanos y Salvos - Backend

Backend completo de "Sanos y Salvos", aplicación para la gestión de mascotas perdidas y encontradas. Sistema modular con arquitectura de microservicios en **Kotlin + Spring Boot 3.4.5**, integrado con **Docker Compose** para orquestación automática de servicios.

**Tecnología Stack:**
- Lenguaje: Kotlin 2.1.10
- Framework: Spring Boot 3.4.5
- Java: JDK 21
- Base de datos: Xano (nube)
- Message Queue: RabbitMQ
- Contenedorización: Docker & Docker Compose
- Build: Gradle 9.4.1

## 📋 Índice

- [Descripción General](#descripción-general)
- [Servicios del Proyecto](#servicios-del-proyecto)
- [Estructura del Código](#estructura-del-código)
- [¿Cómo Funciona el Backend?](#cómo-funciona-el-backend)
- [Ejecución Automática con Docker](#ejecución-automática-con-docker)
- [Inicio Rápido](#inicio-rápido)
- [Pruebas con Postman](#pruebas-con-postman)
- [Integración con Frontend](#integración-con-frontend)
- [Troubleshooting](#troubleshooting)
- [Referencias](#referencias)

---

## Descripción General

Este proyecto utiliza una arquitectura de **microservicios desacoplados** donde:
- El **BFF (Backend for Frontend)** actúa como API Gateway centralizado
- Cada **servicio de dominio** (usuario, mascota, geolocalización, matching) es independiente
- **Docker Compose** orquesta automáticamente todos los servicios en contenedores
- **Xano** es la base de datos autoritativa en la nube
- **RabbitMQ** facilita comunicación asincrónica entre servicios

---

## Servicios del Proyecto

| Servicio | Ruta | Puerto | Estado | Descripción |
|----------|------|--------|--------|-------------|
| **BFF Gateway** | `apps/bff-service` | 8080 | ✅ Activo | Orquesta y adapta respuestas de microservicios |
| **User Service** | `services/user-service` | 8081 | ✅ Activo | Autenticación, registro y gestión de usuarios |
| **Pet Service** | `services/pet-service` | 8082 | ⏳ Futuro | Gestión de mascotas y reportes |
| **Geo Service** | `services/geoservice` | 8083 | ⏳ Futuro | Lógica geoespacial e integración con OSM |
| **Match Service** | `services/match-service` | 8084 | ⏳ Futuro | Motor de coincidencias y notificaciones |
| **RabbitMQ** | Docker | 5672, 15672 | ✅ Activo | Message broker para comunicación asincrónica |

---

## Estructura del Código

```
SanosysalvosV2/
├── app/                           # Módulo Android (Jetpack Compose)
├── apps/
│   └── bff-service/               # API Gateway - Puerto 8080
├── services/
│   └── user-service/              # Microservicio de autenticación - Puerto 8081
├── shared/
│   ├── common/                    # Utilidades transversales
│   └── contracts/                 # DTOs y modelos compartidos
├── docker-compose.yml             # ⭐ Orquestación de servicios
├── build.gradle.kts               # Configuración raíz de Gradle
├── settings.gradle.kts            # Módulos incluidos
└── README.md
```

---

## ¿Cómo Funciona el Backend?

### Flujo de Autenticación (user-service - Puerto 8081)

```
Usuario (App Android)
    ↓
POST /api/v1/users/register
    ↓
BFF Gateway (8080)
    ↓
User Service (8081)
    ↓
Valida y llama a XanoAuthClient
    ↓
Xano (Nube) - Genera uid + token JWT
    ↓
Respuesta: { uid, role, token }
```

**Endpoints Activos:**
- `POST /api/v1/users/register` - Registro de nuevo usuario
- `POST /api/v1/users/login` - Autenticación
- `GET /api/v1/users/health` - Health check del servicio

**Roles:** `USER`, `ADMIN`, `COLLABORATOR`

### Flujo de API Gateway (bff-service - Puerto 8080)

El BFF actúa como **orquestador central** que delega a microservicios:

```
Cliente (App Android)
    ↓
GET /api/v1/bff/health
GET /api/v1/bff/map/provider
GET /api/v1/bff/map/layers
GET /api/v1/bff/map/reports/nearby
    ↓
BFF Service (8080)
    ↓
Delega a:
- User Service (8081)
- Geo Service (8083)
- Match Service (8084)
    ↓
Respuesta agregada y adaptada
```

### Base de Datos - Xano (Nube)

**Xano es la fuente única de verdad (SSOT):**
- ✅ Gestión de usuarios, autenticación y tokens JWT
- ✅ Almacenamiento de mascotas y reportes
- ✅ Índices únicos, transacciones, backups automáticos
- ✅ Accesible desde cualquier ubicación (nube)

**Endpoint:** `https://x8ki-letl-twmt.n7.xano.io/api:sanos-y-salvos-auth`

### Message Queue - RabbitMQ

**Propósito:**
- Comunicación asincrónica entre microservicios
- Desacoplamiento de servicios
- Manejo robusto de fallos y reintentos

**Acceso desde Docker:**
- Puerto AMQP: `5672`
- UI Management: `http://localhost:15672` (usuario: guest, contraseña: guest)

---

## Ejecución Automática con Docker

### ¿Cómo Funciona Docker en Este Proyecto?

**Docker Compose automatiza completamente el ciclo de vida del backend:**

#### 1. Build Multi-etapas (Dockerfile)

- **Etapa 1 (Builder):** Compila el código Gradle dentro del contenedor
  - Instala Gradle 9.4.1 y Java 21
  - Descarga dependencias
  - Compila el código fuente
  - Genera JAR ejecutable (bootJar)

- **Etapa 2 (Runtime):** Copia solo el JAR a una imagen JRE pequeña
  - Base: `eclipse-temurin:21-jre` (~300MB)
  - Copia JAR: `/app/app.jar`
  - Resultado: Imagen optimizada de ~350MB

**Ventajas:**
- No requires Java instalado localmente
- No depende de gradle.properties del host
- Imagen final pequeña y lista para producción

#### 2. Orquestación (docker-compose.yml)

Define 3 servicios que se comunican en red `sanosysalvos-net`:

```yaml
services:
  rabbitmq:
    image: rabbitmq:3-management-alpine
    ports:
      - "5672:5672"      # AMQP
      - "15672:15672"    # Management UI
    
  user-service:
    build:
      context: .
      dockerfile: services/user-service/Dockerfile
    ports:
      - "8081:8081"
    environment:
      - XANO_AUTH_BASE_URL=https://...
    depends_on:
      - rabbitmq
    
  bff-service:
    build:
      context: .
      dockerfile: apps/bff-service/Dockerfile
    ports:
      - "8080:8080"
    environment:
      - GEOSERVICE_BASE_URL=http://geoservice:8083
    depends_on:
      - rabbitmq
```

#### 3. Variables de Entorno

Cada servicio recibe automáticamente:
- `XANO_AUTH_BASE_URL` - Para conectar con Xano
- `GEOSERVICE_BASE_URL` - Para comunicación intra-servicios
- Otras configuraciones específicas

### ¿Manual o Automático?

**✅ AUTOMÁTICO CON DOCKER**

```bash
docker compose up -d --build
```

**Qué ocurre automáticamente:**
1. ✓ Descarga imágenes base (gradle, jre, rabbitmq)
2. ✓ Compila código Gradle dentro del contenedor
3. ✓ Crea imágenes Docker de user-service y bff-service
4. ✓ Levanta 3 contenedores simultáneamente
5. ✓ Configura red interna automáticamente
6. ✓ Los servicios se reinician automáticamente si fallan
7. ✓ Todo listo en ~3-5 minutos

**✅ MANUAL CON GRADLE** (Solo si quieres debugging local)

```bash
./gradlew :services:user-service:bootRun
./gradlew :apps:bff-service:bootRun
```

Requiere:
- Java 21 instalado localmente
- RabbitMQ corriendo por separado (`docker run -d -p 5672:5672 rabbitmq:3`)
- Manejo manual de puertos y variables de entorno

---

## Inicio Rápido

### Requisito Previo
- Docker Desktop instalado y corriendo

### Paso 1: Iniciar Todos los Servicios

```bash
cd c:\Users\onett\AndroidStudioProjects\SanosysalvosV2
docker compose up -d --build
```

**Salida esperada:**
```
✓ Container sanos-y-salvos-rabbitmq  Started (port 5672, 15672)
✓ Container sanos-y-salvos-user      Started (port 8081)
✓ Container sanos-y-salvos-bff       Started (port 8080)
```

### Paso 2: Verificar Estado

```bash
docker ps
```

Deberías ver 3 contenedores en estado `Up`.

### Paso 3: Prueba Rápida de Salud

```bash
# user-service
curl http://localhost:8081/api/v1/users/health

# bff-service
curl http://localhost:8080/api/v1/bff/health

# RabbitMQ UI (usuario: guest, contraseña: guest)
open http://localhost:15672
```

### Paso 4: Detener Servicios

```bash
docker compose down
```

### Paso 5: Ver Logs

```bash
# Logs de todos los servicios
docker compose logs -f

# Logs de un servicio específico
docker compose logs -f user-service
docker compose logs -f bff-service
```

---

## Pruebas con Postman

### Base URL
`http://localhost:8081/api/v1/users`

### 1. Registrar Usuario

**POST** `/register`

```json
{
  "fullName": "Test User",
  "email": "test+local@example.com",
  "password": "P@ssw0rd1",
  "role": "USER"
}
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "uid": "U001",
    "role": "USER",
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 2. Login

**POST** `/login`

```json
{
  "email": "test+local@example.com",
  "password": "P@ssw0rd1"
}
```

### 3. Health Check

**GET** `/health`

```json
{
  "service": "user-service",
  "status": "up"
}
```

### 4. Registrar Colaborador (Clínica Veterinaria)

**POST** `/register`

```json
{
  "fullName": "Clínica San Antón",
  "email": "clinica@sananton.com",
  "password": "P@ssw0rd1",
  "role": "COLLABORATOR",
  "collaboratorType": "VETERINARY_CLINIC"
}
```

---

## Integración con Frontend

### Conectar desde App Android

**Desde el emulador:**
- Host: `10.0.2.2` (alias de localhost en emulador)
- Puerto: `8081` para user-service, `8080` para BFF

**URLs de conexión:**
```kotlin
// User Service (Autenticación)
http://10.0.2.2:8081/api/v1/users/register
http://10.0.2.2:8081/api/v1/users/login

// BFF Service (API Gateway)
http://10.0.2.2:8080/api/v1/bff/health
http://10.0.2.2:8080/api/v1/bff/map/*
```

**Actualiza en RetrofitClient.kt:**
```kotlin
const val BASE_URL = "http://10.0.2.2:8081/api/v1/users/"
```

---

## Troubleshooting

### ❌ Error: "Docker daemon not running"

**Solución:** Inicia Docker Desktop

```powershell
Start-Process "C:\Program Files\Docker\Docker\Docker.exe"
```

---

### ❌ Error: "Port 8081 already in use"

```powershell
# Encontrar proceso
netstat -ano | findstr ":8081"

# Matar proceso (reemplaza PID)
taskkill /PID 12345 /F

# O cambiar puerto en docker-compose.yml
```

---

### ❌ Error: "Connection refused at localhost:8081"

```bash
# Verificar que contenedores están corriendo
docker ps

# Ver logs
docker compose logs user-service

# Esperar 10-15 segundos post-startup para que el servicio esté listo
```

---

### ❌ Error: "Duplicate record detected"

**Causa:** Email ya existe en Xano

**Solución:** Usa otro email:
```
test+$(date +%s)@example.com
```

---

## Comandos Útiles

```bash
# Construir imágenes sin levantar servicios
docker compose build --no-cache

# Rebuild y restart
docker compose down
docker compose up -d --build

# Ver recursos usados
docker stats

# Entrar a un contenedor
docker exec -it sanos-y-salvos-user bash

# Limpiar todo (contenedores + volúmenes)
docker compose down -v

# Ver logs completos
docker compose logs user-service --tail=100
```

---

## Referencias

- **Xano API Docs:** https://x8ki-letl-twmt.n7.xano.io/api:workspace:t1gH6k-I
- **Spring Boot:** https://spring.io/projects/spring-boot
- **Docker Compose:** https://docs.docker.com/compose/
- **Kotlin:** https://kotlinlang.org/

---

**Última actualización:** Mayo 2026
**Versión:** 1.0.0 - Docker Compose Edition
