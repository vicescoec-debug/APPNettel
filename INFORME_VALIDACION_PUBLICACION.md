# Informe de validaciÃ³n para publicaciÃ³n â€” Nettel MarÃ­timo

**Fecha:** 3 de agosto de 2026  
**VersiÃ³n revisada:** `1.0-release`  
**Version Code:** `51`  
**Package debug probado:** `com.nettel.maritimo.next.debug`  
**Package release esperado:** `com.nettel.maritimo.next`

## ValidaciÃ³n ejecutada

Comando:

```powershell
$env:JAVA_HOME='C:\Users\espin\Documents\APP Nettel\.tools\jdk17\jdk-17.0.19+10'
$env:ANDROID_HOME='C:\Users\espin\Documents\APP Nettel\.tools\android-sdk'
.\gradlew.bat --no-daemon assembleDebug testDebugUnitTest --stacktrace
```

Resultado:

```text
BUILD SUCCESSFUL
38 actionable tasks: 5 executed, 33 up-to-date
```

## ConfiguraciÃ³n revisada

```text
compileSdk: 35
targetSdk: 35
minSdk: 26
versionCode: 51
versionName: 1.0
API base: https://spot1.nettelcorp.com/api/v1/
```

## Manifest

Permisos:

```text
INTERNET
ACCESS_NETWORK_STATE
POST_NOTIFICATIONS
```

Seguridad:

- `usesCleartextTraffic=false`.
- `allowBackup=false`.
- `fullBackupContent=false`.
- Activities internas no exportadas.
- Activity launcher exportada correctamente.

## Estado funcional

MÃ³dulos implementados:

- Splash / bienvenida.
- Login.
- RecuperaciÃ³n de contraseÃ±a.
- Cambio de contraseÃ±a.
- MenÃº principal.
- Dispositivos.
- Mapa de Flota.
- HistÃ³rico.
- Alertas.
- Usuarios / perfil logoneado.
- ConfiguraciÃ³n.

## Riesgos o pendientes antes de producciÃ³n

1. Falta generar `.aab` release firmado.
2. Falta URL pÃºblica HTTPS de polÃ­tica de privacidad.
3. Falta completar Seguridad de Datos en Play Console.
4. Falta cuenta demo para revisiÃ³n.
5. Endpoint `POST /users/update` no confirmado en backend.
6. `targetSdk 35` es vÃ¡lido para la ventana actual, pero debe migrarse a API 36 antes del 31 de agosto de 2026 para nuevas subidas/actualizaciones.
7. Las capturas de Play Store deben revisarse para no exponer datos personales reales.

## ConclusiÃ³n tÃ©cnica

La versiÃ³n `1.0-release` estÃ¡ validada para pruebas internas. Para publicaciÃ³n en Google Play se debe preparar una variante `release` firmada y subir un `.aab`, no el APK debug.

## Ajuste de presentaciÃ³n pÃºblica

Se actualizÃ³ `ConfiguraciÃ³n > Acerca de` para que muestre informaciÃ³n orientada al usuario final:

- DescripciÃ³n funcional de la app sin referencia tÃ©cnica `REST + JSON`.
- VersiÃ³n visible: `1.0`.
- TÃ©rminos y condiciones como enlace clicable a `https://nettelcorp.com/privacidad.html`.


## Firma release generada

Se generó un Android App Bundle firmado para publicación:

`C:\Users\espin\Documents\APP Nettel\Nettel Maritimo v1.0-release-signed.aab`

La keystore/upload key queda archivada únicamente de forma local en:

`C:\Users\espin\Documents\APP Nettel\.signing\APPNettel`

Certificado SHA-256:

`79:B7:6A:0E:D9:3A:23:3E:C9:8B:BB:FF:80:8F:53:3C:1E:AD:1D:1D:45:FF:78:8E:1C:78:44:62:65:D2:07:B1`

Ver detalle en `FIRMA_PUBLICACION_GOOGLE_PLAY.md`.
