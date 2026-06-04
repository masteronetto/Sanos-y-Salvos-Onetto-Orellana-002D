# Sanos y Salvos V2

Aplicación con arquitectura de microservicios (Kotlin + Spring Boot) y cliente Android (Jetpack Compose).

El cliente Android consume directamente la API de Xano (x8ki-letl-twmt.n7.xano.io) eliminando la necesidad de infraestructura de tuneles.

## Estado Actual

### Backend API

El proyecto ahora usa **Xano** como backend API centralizado:
- Endpoint base: `https://x8ki-letl-twmt.n7.xano.io/`
- Endpoints disponibles:
  - Auth: `/api:sanos-y-salvos-auth/{login|register}`
  - Users: `/api:sanos-y-salvos-users/list`
  - Maps: `/api:maps/{provider|layers|reports/nearby}`

### Android Client

- Build compilado exitosamente con rutas Xano correctas
- Cliente Android consume directamente endpoints Xano (sin BFF intermediario)
- Retrofit configurado con rutas HTTP válidas (`/api:sanos-y-salvos-auth/...`)
- AuthRepository con mapeo flexible para respuestas Xano
- Listo para pruebas en celular físico desde Android Studio

### Backend Local (Opcional)

Los servicios locales en Docker Compose están disponibles pero NO son requeridos para funcionar el cliente Android. Se usan solo para desarrollo backend:
- bff-service: 8080
- user-service: 8081
- pet-service: 8082
- geoservice: 8083
- match-service: 8084
- rabbitmq: 5672, 15672

## Arquitectura

- app: cliente Android.
- apps/bff-service: gateway BFF.
- services/user-service: autenticacion y usuarios.
- services/pet-service: dominio de mascotas/reportes.
- services/geoservice: geodatos y capas de mapa.
- services/match-service: matching y reglas de coincidencia.
- shared/common y shared/contracts: librerias compartidas.

## Requisitos

- Android Studio (con Android SDK para emulador o dispositivo físico)
- Java 17
- Gradle Wrapper (incluido)
- Conexión a internet (para acceder a Xano API)

## Inicio Rápido: Pruebas en Android Studio

### 1) Abrir proyecto en Android Studio

```powershell
# Desde el directorio del proyecto
start . # Abre el proyecto en Android Studio
```

### 2) Configurar emulador o dispositivo

**Emulador**: Android Studio → Device Manager → Crear/Iniciar emulador  
**Dispositivo físico**: Conectar por USB, habilitar "USB Debugging" en Settings > Developer Options

### 3) Ejecutar la aplicación

En Android Studio:
1. Select Run > Run 'app' o presiona Shift+F10
2. Seleccionar target (emulador o dispositivo)
3. Esperar a que compile e instale

### 4) Testear flujo de login

- Email: `admin+local@example.com`
- Password: `P@ssw0rd1`

Debería ver:
1. Pantalla de login
2. Conexión exitosa a Xano (`/api:sanos-y-salvos-auth/login`)
3. Acceso a panel de admin o pantalla de maps

## Backend Local (Opcional)

Los servicios microservicios en Docker Compose NO son requeridos para el client Android, ya que consume Xano directamente. Para desarrollo backend:

```powershell
docker compose up -d
```

Verifica salud:

```powershell
$ports = 8080,8081,8082,8083,8084
foreach ($p in $ports) {
  try {
    $r = Invoke-WebRequest -Uri "http://localhost:$p/actuator/health" -TimeoutSec 8 -UseBasicParsing
    Write-Output "PORT $p -> $($r.StatusCode)"
  } catch {
    Write-Output "PORT $p -> FAIL"
  }
}
```

## Android: Build APK (Manual)

Si necesitas generar APK directamente (sin Android Studio):

```powershell
.\gradlew :app:assembleDebug --no-daemon
```

APK ubicado en: `app/build/outputs/apk/debug/app-debug.apk`

Instalar en dispositivo:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk

## Troubleshooting

### Error de jlink al compilar Android

Sintoma comun:

- jlink executable ... redhat.java ... does not exist

Accion aplicada en workspace:

- .vscode/settings.json forzado a JBR de Android Studio para Java LS e import/Gradle.

Si reaparece en UI de VS Code:

1. Reload Window en VS Code.
2. Reintentar build con Gradle Wrapper.

### Login tarda y luego falla por timeout

Causas mas probables:

- Backend no levantado.
- Red distinta entre celular y PC.
- Firewall bloqueando 8080/8081.

Validar primero backend local con el bloque de health checks de este README.

## Politica de Archivos Generados

No se deben subir artefactos generados de build (por ejemplo carpetas bin y build), porque:

- no son fuente de verdad,
- ensucian PRs con ruido,
- aumentan conflictos y tamano del repositorio.

El repositorio debe contener codigo fuente, configuraciones y scripts reproducibles.

## Comandos Utiles

```powershell
# apagar stack
docker compose down

# logs de todos los servicios
docker compose logs -f

# logs de un servicio
docker compose logs -f user-service

# detener daemons gradle
.\gradlew --stop
```

## Pendiente por Hacer

### Testing & Validación

- [ ] **Pruebas funcionales en celular físico desde Android Studio**
  - [ ] Flujo de login exitoso con Xano
  - [ ] Acceso a panel admin
  - [ ] Carga de mapa y datos de ubicación
  - [ ] Validar campos de respuesta Xano (token, userId, role)
  
- [ ] **Validar formato de respuesta Xano**
  - [ ] Asegurar que AuthRepository.mapToAuthResponse() maneja todos los casos
  - [ ] Verificar parsing de `/api:sanos-y-salvos-auth/login` response
  - [ ] Revisar campos adicionales que pueda retornar Xano (collaboratorType, etc)

### Features Faltantes

- [ ] **Pantalla de Maps completa**
  - [ ] Mostrar provider (Mapbox/Google)
  - [ ] Cargar capas de mapa
  - [ ] Mostrar reportes cercanos con ubicación usuario
  
- [ ] **Pantalla de Registro**
  - [ ] Implementar flujo de `/api:sanos-y-salvos-auth/register`
  - [ ] Validaciones de formulario (email, password strength)
  - [ ] Manejo de errores de registro duplicado

- [ ] **Integración de Mascotas & Reportes**
  - [ ] Pantalla de "Mis Mascotas" (llamar a Xano pets endpoint)
  - [ ] Crear reporte de mascota perdida
  - [ ] Mostrar reportes activos/resueltos

- [ ] **Mensajería/Chat**
  - [ ] Implementar pantalla de mensajes
  - [ ] Integración con endpoint de mensajes Xano
  
- [ ] **User Profile**
  - [ ] Pantalla de perfil de usuario
  - [ ] Editar información personal
  - [ ] Foto de perfil

### Backend

- [ ] **Sincronización BFF-Xano (Opcional)**
  - Si se desea mantener BFF como cache/wrapper, sincronizar endpoints con Xano
  - Actualmente Android llama directo a Xano (recomendado para MVP)

- [ ] **Integración RabbitMQ**
  - [ ] Verificar flujos de evento entre servicios
  - [ ] Pruebas de integración end-to-end

### DevOps & Deployment

- [ ] **Configurar CI/CD**
  - [ ] Pipeline de build Android en CI
  - [ ] Automatic testing en pull requests
  
- [ ] **Documentar Xano API Contract**
  - [ ] Guía de endpoints Xano disponibles
  - [ ] Formato de requests/responses esperados
  - [ ] Credenciales y acceso a workspace Xano
  
- [ ] **Preparar deployment**
  - [ ] Configurar release build (ProGuard rules)
  - [ ] Signing de APK para Google Play (si aplica)

### Limpieza Técnica

- [ ] **Remover código no usado**
  - RetrofitClient.kt (deprecated)
  - NetworkConfig.kt (deprecated)
  - Referencias a BFF si Android no las usa

- [ ] **Consolidar configuración**
  - Centralizar base URLs (gradle.properties vs BuildConfig)
  - Documentar dónde vive configuración por environment (dev/staging/prod)

---

Última actualización: 2026-06-04

