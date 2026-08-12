# Paquete de publicaciÃ³n Google Play â€” Nettel MarÃ­timo

**Fecha:** 3 de agosto de 2026  
**VersiÃ³n revisada:** `1.0`  
**Version Code:** `51`  
**Estado:** lista para pruebas internas; pendiente generaciÃ³n release firmada `.aab`.

## Archivos principales

APK debug validado:

```text
C:\Users\espin\Documents\APP Nettel\Nettel Maritimo v1.0-release.apk
```

Proyecto:

```text
C:\Users\espin\Documents\APP Nettel\NettelMaritimoNext
```

Fuentes actualizadas:

```text
C:\Users\espin\Documents\APP Nettel\Fuentes_Actualizados_NettelMaritimoNext
```

ImÃ¡genes:

```text
C:\Users\espin\Documents\APP Nettel\PlayStore_Imagenes
```

## DocumentaciÃ³n generada/actualizada

- `Resumen_Tecnico_APP_Nettel_Maritimo.dm`
- `VERSION_GOOGLE_PLAY_NETTEL_REST.md`
- `NettelMaritimoNext/MODULARIZACION_PROYECTO.md`
- `NettelMaritimoNext/PLAY_STORE_CHECKLIST.md`
- `NettelMaritimoNext/PRIVACY_POLICY_DRAFT.md`
- `NettelMaritimoNext/README.md`
- `GOOGLE_PLAY_DATA_SAFETY.md`
- `RELEASE_NOTES_GOOGLE_PLAY.md`
- `INFORME_VALIDACION_PUBLICACION.md`
- `GOOGLE_PLAY_PUBLICACION_NETTEL_MARITIMO.md`

## Resultado de validaciÃ³n

Comando ejecutado:

```powershell
.\gradlew.bat --no-daemon assembleDebug testDebugUnitTest --stacktrace
```

Resultado:

```text
BUILD SUCCESSFUL
```

## RevisiÃ³n de cumplimiento

Puntos favorables:

- HTTPS obligatorio.
- No cleartext traffic.
- Menos permisos que la app legacy.
- No usa ubicaciÃ³n del telÃ©fono.
- No usa cÃ¡mara, micrÃ³fono, contactos ni almacenamiento externo.
- No usa publicidad.
- SesiÃ³n cifrada.
- Target SDK 35.
- App funcional con login y backend REST.

Puntos pendientes antes de publicaciÃ³n:

1. Generar `.aab` release firmado.
2. Publicar polÃ­tica de privacidad por HTTPS.
3. Completar Seguridad de Datos en Play Console.
4. Cargar cuenta demo para revisiÃ³n.
5. Confirmar endpoint `POST /api/v1/users/update` si se mantendrÃ¡ ediciÃ³n de correo/celular.
6. Revisar capturas para ocultar datos personales reales.
7. Migrar a target SDK 36 antes del 31 de agosto de 2026 si se subirÃ¡ despuÃ©s de esa fecha.

## Comando release esperado

DespuÃ©s de configurar keystore:

```powershell
$env:JAVA_HOME='C:\Users\espin\Documents\APP Nettel\.tools\jdk17\jdk-17.0.19+10'
$env:ANDROID_HOME='C:\Users\espin\Documents\APP Nettel\.tools\android-sdk'
.\gradlew.bat bundleRelease
```

Salida esperada:

```text
NettelMaritimoNext\app\build\outputs\bundle\release\app-release.aab
```

## Cuenta demo requerida por Google Play

Completar antes de subir:

```text
Usuario:
ContraseÃ±a:
Instrucciones:
```

RecomendaciÃ³n: usar una cuenta con datos de prueba o datos expresamente autorizados para revisiÃ³n.

## ConclusiÃ³n

La versiÃ³n `1.0` estÃ¡ lista como base de publicaciÃ³n, pero Google Play requiere un `.aab` release firmado y completar metadatos/polÃ­ticas en Play Console. El APK debug no debe subirse como versiÃ³n final.

## ActualizaciÃ³n de pantalla Acerca de

La opciÃ³n `ConfiguraciÃ³n > Acerca de` fue ajustada para presentaciÃ³n pÃºblica:

- Se eliminÃ³ del texto visible la referencia tÃ©cnica `REST + JSON`.
- El apartado `VersiÃ³n` muestra: `1.0`.
- El enlace de tÃ©rminos y condiciones abre `https://nettelcorp.com/privacidad.html`.

