# Checklist de publicaciÃ³n en Google Play â€” Nettel MarÃ­timo

**Fecha:** 3 de agosto de 2026  
**VersiÃ³n revisada:** `1.0-release`  
**Version Code:** `51`  
**Package release:** `com.nettel.maritimo.next`  
**Package debug probado:** `com.nettel.maritimo.next.debug`  
**Target SDK actual:** `35`

## Estado tÃ©cnico

- [x] Compila correctamente con `assembleDebug`.
- [x] Pruebas unitarias debug ejecutadas con Ã©xito.
- [x] API base por HTTPS: `https://spot1.nettelcorp.com/api/v1/`.
- [x] `usesCleartextTraffic=false`.
- [x] Componentes internos `exported=false`.
- [x] Ãšnica activity exportada: `SplashActivity` con launcher.
- [x] `allowBackup=false`.
- [x] No usa Google Maps.
- [x] Mapa migrado a OpenStreetMap/OpenSeaMap mediante `osmdroid`.
- [x] Login, recuperaciÃ³n, cambio de contraseÃ±a, dispositivos, mapa, histÃ³rico, alertas y usuarios implementados.
- [x] Notificaciones mediante WorkManager.
- [x] SesiÃ³n almacenada con `EncryptedSharedPreferences`.
- [x] Textos crÃ­ticos corregidos y legibles.

## Pendiente obligatorio antes de subir a Play

- [ ] Generar keystore release definitivo.
- [ ] Configurar firma release fuera del repositorio.
- [ ] Generar Android App Bundle `.aab`.
- [ ] Probar el `.aab` en canal interno de Google Play.
- [ ] Publicar polÃ­tica de privacidad en una URL HTTPS pÃºblica.
- [ ] Completar formulario de Seguridad de Datos en Play Console.
- [ ] Proporcionar cuenta demo funcional para revisiÃ³n de Google Play.
- [ ] Confirmar que el endpoint de actualizaciÃ³n de correo/celular estÃ¡ habilitado:
  - recomendado: `POST /api/v1/users/update`.
- [ ] Confirmar comportamiento final de recuperaciÃ³n de contraseÃ±a con datos reales.

## Requisito de target API

Estado al 3 de agosto de 2026:

- La app apunta a `targetSdk 35`.
- SegÃºn documentaciÃ³n oficial de Android/Google Play, desde el 31 de agosto de 2026 las apps nuevas y actualizaciones deberÃ¡n apuntar a Android 16 / API 36 o superior.
- Si se sube antes de esa fecha, `targetSdk 35` es aceptable para la ventana actual.
- RecomendaciÃ³n: preparar migraciÃ³n a `compileSdk 36` / `targetSdk 36` antes del 31 de agosto de 2026.

Fuentes consultadas:

- [Target API level requirements](https://developer.android.com/google/play/requirements/target-sdk)
- [Play Console Requirements](https://support.google.com/googleplay/android-developer/answer/10788890)
- [User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311)

## Permisos declarados

```xml
android.permission.INTERNET
android.permission.ACCESS_NETWORK_STATE
android.permission.POST_NOTIFICATIONS
```

ObservaciÃ³n:

- La app muestra posiciones de embarcaciones consultadas desde servidor, pero no solicita permisos de ubicaciÃ³n del telÃ©fono.
- No usa cÃ¡mara, micrÃ³fono, contactos, almacenamiento externo ni telÃ©fono.

## Seguridad de datos

Completar en Play Console segÃºn el documento:

```text
GOOGLE_PLAY_DATA_SAFETY.md
```

Datos tratados:

- Datos de cuenta: usuario, nombre, identificaciÃ³n, correo, telÃ©fono.
- Datos operativos: dispositivos/embarcaciones, matrÃ­cula, estado, alertas.
- UbicaciÃ³n de embarcaciones/dispositivos marÃ­timos desde backend.
- Identificador de dispositivo Android para login/sesiÃ³n.

La app transmite por HTTPS y almacena localmente solo datos necesarios para sesiÃ³n, cachÃ© de alertas y cachÃ© de mapas.

## Material grÃ¡fico

Ver:

```text
C:\Users\espin\Documents\APP Nettel\PlayStore_Imagenes
```

Pendiente:

- [ ] Revisar que ninguna captura muestre datos personales reales sensibles.
- [ ] Preparar grÃ¡fico destacado 1024x500 si aÃºn no estÃ¡ generado.
- [ ] Usar icono 512x512 existente:

```text
app/src/main/res/drawable-nodpi/play_store_icon_512.png
```

## Cuenta demo para revisiÃ³n

Google Play exige credenciales y recursos para revisar apps con login.

Pendiente definir:

```text
Usuario demo:
ContraseÃ±a demo:
Notas para revisiÃ³n:
```

La cuenta debe tener datos ficticios o autorizados para revisiÃ³n.

## Release recomendado

1. Crear archivo `keystore.properties` fuera del control de versiones.
2. Configurar signingConfig release.
3. Generar:

```powershell
$env:JAVA_HOME='C:\Users\espin\Documents\APP Nettel\.tools\jdk17\jdk-17.0.19+10'
$env:ANDROID_HOME='C:\Users\espin\Documents\APP Nettel\.tools\android-sdk'
.\gradlew.bat bundleRelease
```

4. Subir:

```text
app/build/outputs/bundle/release/app-release.aab
```

## DecisiÃ³n de publicaciÃ³n

La app estÃ¡ funcionalmente lista para pruebas internas. Para publicaciÃ³n pÃºblica en Play Store, faltan tareas externas al APK:

- firma release/AAB;
- URL pÃºblica de polÃ­tica de privacidad;
- formulario de Seguridad de Datos;
- cuenta demo;
- endpoint de actualizaciÃ³n de perfil si se quiere habilitar ediciÃ³n de correo/celular en producciÃ³n.

