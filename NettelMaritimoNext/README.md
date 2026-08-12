# Nettel MarÃ­timo

AplicaciÃ³n Android para monitoreo marÃ­timo de flota, dispositivos, histÃ³rico de posiciones y alertas.

## VersiÃ³n actual

```text
versionName: 1.0
versionCode: 51
package debug: com.nettel.maritimo.next.debug
package release: com.nettel.maritimo.next
minSdk: 26
targetSdk: 35
compileSdk: 35
```

## Funciones incluidas

- Login y cierre de sesiÃ³n mediante REST + JSON.
- RecuperaciÃ³n de contraseÃ±a.
- Cambio obligatorio de contraseÃ±a temporal.
- Perfil del usuario logoneado con correo y celular.
- Consulta de dispositivos asignados.
- Mapa de flota con OpenSeaMap/osmdroid.
- BatimetrÃ­a opcional.
- Capa de pesca marÃ­tima.
- Vista satelital.
- HistÃ³rico de posiciones.
- Alertas operativas y notificaciones periÃ³dicas.
- Alertas crÃ­ticas SOS/PT1.
- Alertas de baterÃ­a baja.
- Marcado de alertas como leÃ­das.
- CachÃ© de alertas separado por usuario/flota.
- Almacenamiento cifrado de sesiÃ³n.

## Backend

API principal:

```text
https://spot1.nettelcorp.com/api/v1/
```

Contrato:

```text
REST_JSON_API_SPOT2.md
```

## CompilaciÃ³n debug

```powershell
$env:JAVA_HOME='C:\Users\espin\Documents\APP Nettel\.tools\jdk17\jdk-17.0.19+10'
$env:ANDROID_HOME='C:\Users\espin\Documents\APP Nettel\.tools\android-sdk'
.\gradlew.bat --no-daemon assembleDebug testDebugUnitTest --stacktrace
```

## PublicaciÃ³n

Para Google Play se debe generar un `.aab` release firmado. Ver:

```text
PLAY_STORE_CHECKLIST.md
GOOGLE_PLAY_DATA_SAFETY.md
INFORME_VALIDACION_PUBLICACION.md
RELEASE_NOTES_GOOGLE_PLAY.md
```

