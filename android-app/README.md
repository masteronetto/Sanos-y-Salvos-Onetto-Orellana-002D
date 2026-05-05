Android app dentro del repositorio "Sanos y Salvos".

Estructura principal:

- `app/` : módulo Android con Jetpack Compose
- `data/api` : Retrofit client y API
- `data/repository` : Repositorio para llamadas de red
- `model` : modelos de datos (RegisterRequest, LoginRequest, AuthResponse)
- `ui` : pantallas y componentes
- `viewmodel` : `AuthViewModel`

IMPORTANTE: Cambia `BASE_URL` en `RetrofitClient.kt` por la URL pública de tu BFF.

## Comandos de build y su función

A continuación se listan los comandos Gradle más útiles para compilar y testear el módulo `android-app/` desde la raíz del repositorio. Ejecuta estos comandos en la terminal desde la raíz del repo.

- Limpiar build (elimina archivos de compilación):

```bash
./gradlew -p android-app clean
```

- Compilar variante `debug` (genera APK de debug):

```bash
./gradlew -p android-app assembleDebug
```

- Compilar variante `release` (genera APK de release, requiere signing config si aplica):

```bash
./gradlew -p android-app assembleRelease
```

- Ejecutar pruebas unitarias de la variante `debug`:

```bash
./gradlew -p android-app testDebugUnitTest
```

- Ejecutar todos los tests disponibles (si los hay):

```bash
./gradlew -p android-app test
```

- Ejecutar lint (análisis estático):

```bash
./gradlew -p android-app lint
```

- Ejecutar con más información de depuración (útil para diagnosticar errores):

```bash
./gradlew -p android-app assembleDebug --info --stacktrace
```

Notas rápidas:

- Asegúrate de tener instalado JDK 17 y el Android SDK/NDK requeridos por `app/build.gradle.kts`.
- Si prefieres cambiar de directorio y usar el wrapper del módulo:

```bash
cd android-app
../gradlew assembleDebug
```

