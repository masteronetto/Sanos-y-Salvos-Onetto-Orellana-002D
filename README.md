# Sanos y Salvos V2

Aplicacion con arquitectura de microservicios (Kotlin + Spring Boot) y cliente Android (Jetpack Compose).

Este README refleja el estado actual del proyecto y deja un flujo de trabajo claro para continuar pruebas.

## Estado Actual

### Backend

Servicios levantados y verificados en Docker Compose:

- bff-service: 8080
- user-service: 8081
- pet-service: 8082
- geoservice: 8083
- match-service: 8084
- rabbitmq: 5672, 15672

Verificacion realizada con health checks locales en 8080-8084: respuesta 200 y status UP.

### Android

- Build Android debug exitoso.
- Correccion aplicada para compilacion con jlink en VS Code.
- Login preparado para celular fisico:
  - Se elimino la seleccion manual de IP en pantalla de login.
  - Se agrego recuperacion automatica de host backend cuando falla conectividad.
  - Se mejoro el comportamiento de timeout para failover mas rapido.

## Arquitectura

- app: cliente Android.
- apps/bff-service: gateway BFF.
- services/user-service: autenticacion y usuarios.
- services/pet-service: dominio de mascotas/reportes.
- services/geoservice: geodatos y capas de mapa.
- services/match-service: matching y reglas de coincidencia.
- shared/common y shared/contracts: librerias compartidas.

## Requisitos

- Docker Desktop.
- Java 17 para Android build local.
- Gradle Wrapper (incluido en el repositorio).

## Inicio Rapido

### 1) Levantar backend

```powershell
docker compose up -d --no-build
```

Si necesitas reconstruir imagenes:

```powershell
docker compose up -d --build
```

### 2) Verificar contenedores

```powershell
docker compose ps
```

### 3) Verificar salud de servicios

```powershell
$ports = 8080,8081,8082,8083,8084
foreach ($p in $ports) {
  try {
    $r = Invoke-WebRequest -Uri "http://localhost:$p/actuator/health" -TimeoutSec 8 -UseBasicParsing
    Write-Output "PORT $p -> $($r.StatusCode) $($r.Content)"
  } catch {
    try {
      $r2 = Invoke-WebRequest -Uri "http://localhost:$p/health" -TimeoutSec 8 -UseBasicParsing
      Write-Output "PORT $p -> $($r2.StatusCode) $($r2.Content)"
    } catch {
      Write-Output "PORT $p -> FAIL $($_.Exception.Message)"
    }
  }
}
```

## Android: Build e Instalacion

### Build APK debug

```powershell
.\gradlew :app:assembleDebug --no-daemon
```

APK generado en:

- app/build/outputs/apk/debug/app-debug.apk

### Nota para pruebas en celular fisico

- El telefono no puede usar 10.0.2.2 (eso es solo emulador).
- La app ahora intenta resolver host backend automaticamente ante fallos de conexion.
- Aun debes cumplir condiciones de red:
  - PC y celular en la misma red (o usar ADB reverse por USB).
  - Firewall de Windows permitiendo puertos 8080 y 8081.
  - Contenedores realmente levantados.

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

## Proximo Paso (Sesion Siguiente)

- Ejecutar pruebas funcionales completas desde celular:
  - login,
  - flujo admin,
  - carga de mapa,
  - llamadas BFF y servicios de dominio.

---

Ultima actualizacion: 2026-06-03
