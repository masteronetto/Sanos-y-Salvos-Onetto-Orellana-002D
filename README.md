# Sanos y Salvos V2

Repositorio híbrido con cliente Android (Jetpack Compose) y servicios backend en Kotlin/Spring Boot.

El cliente Android consume directamente la API de Xano (`https://x8ki-letl-twmt.n7.xano.io/`), lo que reduce la necesidad de infraestructura de túneles en desarrollo.

## Estado Actual

### Backend API

El proyecto usa **Xano** como backend API centralizado desde Android y desde la mayoría de los servicios de backend.
- Endpoint base: `https://x8ki-letl-twmt.n7.xano.io/`
- Rutas activas en Android:
  - Auth: `/api:sanos-y-salvos-auth/login`, `/api:sanos-y-salvos-auth/register`, `/api:sanos-y-salvos-auth/me`, `/api:sanos-y-salvos-auth/logout`
  - Usuarios/Perfil: `/api:sanos-y-salvos-users/...`
  - Mascotas: `/api:sanos-y-salvos-pets/...`
  - Reportes: `/api:sanos-y-salvos-reports/...`
  - Mapas: `/api:maps/reports/nearby`
  - Coincidencias: `/api:sanos-y-salvos-matches/...`

### Android Client

- Login y registro funcionan contra Xano.
- Pantalla de mapas con reportes cercanos cargados desde Xano.
- Perfil de usuario soporta lectura y edición de datos via Xano.
- Gestión de mascotas: listado, detalle, creación, edición y eliminación.
- Reportes de usuario: listado, detalle, creación y edición.
- Navegación de usuario y admin disponible en la app.
- Retrofit configurado con `BuildConfig.XANO_BASE_URL` para llamadas directas a Xano.

### Backend Local (Opcional)

El stack local con Docker Compose está disponible para desarrollo backend y para el BFF.
- El cliente Android en dispositivo físico no lo requiere para flujos de usuario básicos.
- El BFF local se usa principalmente para rutas de administración y pruebas desde emulador/local.
- Servicios locales opcionales:
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
```

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

## Estado de Implementación

### Funcionalidad ya implementada

- Autenticación y registro en Xano.
- Pantalla de mapas con reportes cercanos y marcadores Xano.
- Perfil de usuario con consulta y actualización de datos.
- Gestión de mascotas: listar, ver detalle, crear, editar y eliminar.
- Reportes de usuario: listar, crear, editar y ver detalle.
- Sección admin completa con dashboard, usuarios, mascotas, reportes, coincidencias, entidades y estadísticas.
- Notificaciones push con Firebase Messaging.
- Navegación separada para usuarios y administradores.

### Estado actual de desarrollo

- La app Android está orientada a uso directo con Xano en dispositivo físico y emulador.
- El BFF local permanece disponible para administración y pruebas locales, pero no es obligatorio para los flujos de usuario básicos.
- Se conservan rutas y servicios backend locales para evaluación y desarrollo complementario.

### Siguientes pasos y mejoras

- Refinar acciones de contacto en la pantalla de coincidencias.
- Mejorar el detalle de colaboradores / contacto directo.
- Consolidar la documentación de configuración entre Xano y BFF.
- Mantener el código limpio eliminando rutas BFF inactivas si el enfoque final es Xano directo.

---

Última actualización: 2026-06-17

