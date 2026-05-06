# 📱 Ejecutar Sanos y Salvos V2 en el Emulador

## ✅ Estado Actual
- **Build**: COMPLETADO EXITOSAMENTE
- **APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Paquete**: `com.example.sanosysalvosv2`

## 🚀 Pasos para Ejecutar

### 1. **Abrir el Emulador de Android Studio**
   - Abre **Android Studio**
   - Click en **Tools** → **Device Manager** (o **AVD Manager**)
   - Selecciona un emulador y haz click en **Play** ▶️
   - Espera a que el emulador se cargue completamente

### 2. **Instalar y Ejecutar la App (PowerShell)**
   
```powershell
# Navega a la carpeta del proyecto
cd C:\Users\onett\AndroidStudioProjects\SanosysalvosV2

# Opción A: Usar gradlew (recomendado)
./gradlew.bat installDebug
./gradlew.bat app:tasks --quiet | findstr /I "launch"

# Opción B: Usar adb directamente
$env:ANDROID_SDK_ROOT = "C:\Users\onett\AppData\Local\Android\Sdk"
& "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk
& "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" shell am start -n "com.example.sanosysalvosv2/com.example.sanosysalvosv2.MainActivity"

# Ver logs en tiempo real
& "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" logcat
```

### 3. **Alternativa desde Android Studio (más simple)**
   - Una vez compilada la app (`./gradlew assembleDebug`)
   - Haz click en **Run** → **app** (o presiona Shift+F10)
   - Android Studio detectará el emulador automáticamente

## 🔧 Troubleshooting

### Emulador no aparece
```powershell
$env:ANDROID_SDK_ROOT = "C:\Users\onett\AppData\Local\Android\Sdk"
& "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" devices
```

### Limpiar datos de la app
```powershell
$env:ANDROID_SDK_ROOT = "C:\Users\onett\AppData\Local\Android\Sdk"
& "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" shell pm clear com.example.sanosysalvosv2
```

### Desinstalar la app
```powershell
$env:ANDROID_SDK_ROOT = "C:\Users\onett\AppData\Local\Android\Sdk"
& "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" uninstall com.example.sanosysalvosv2
```

## 📝 Estructura del Proyecto (Intacta)
```
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/sanosysalvosv2/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/
│   │   │   ├── model/
│   │   │   ├── ui/
│   │   │   └── viewmodel/
│   │   └── res/ (recursos)
│   ├── androidTest/
│   └── test/
└── build.gradle.kts

services/
├── user-service/
└── ... (backends)

shared/
├── common/
└── contracts/
```

## ✨ Cambios Realizados
1. ✅ Corregido error de Gradle `UnknownTaskException` 
2. ✅ Configurado JDK-17 con soporte para `jlink`
3. ✅ Build exitoso: `assembleDebug` completado
4. ✅ APK compilado y listo para instalar

---
**Fecha**: Mayo 6, 2026  
**Estado**: ✅ Listo para emulador
