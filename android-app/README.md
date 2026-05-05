Android app dentro del repositorio "Sanos y Salvos".

Estructura principal:

- `app/` : módulo Android con Jetpack Compose
- `data/api` : Retrofit client y API
- `data/repository` : Repositorio para llamadas de red
- `model` : modelos de datos (RegisterRequest, LoginRequest, AuthResponse)
- `ui` : pantallas y componentes
- `viewmodel` : `AuthViewModel`

IMPORTANTE: Cambia `BASE_URL` en `RetrofitClient.kt` por la URL pública de tu BFF.
