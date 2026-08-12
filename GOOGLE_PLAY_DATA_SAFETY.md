# Seguridad de Datos â€” Google Play Console

**App:** Nettel MarÃ­timo  
**VersiÃ³n revisada:** `1.0-release`  
**Fecha:** 3 de agosto de 2026

Este documento sirve como guÃ­a para completar el formulario de Seguridad de Datos de Google Play Console. Debe ser revisado por el responsable legal y de protecciÃ³n de datos antes de enviar la app.

## Resumen de tratamiento

La app es una herramienta privada/autenticada para monitoreo marÃ­timo. Requiere login y consulta datos asociados al usuario/cliente autorizado.

La app:

- transmite datos por HTTPS;
- no vende datos personales;
- no muestra publicidad;
- no usa SDKs de analÃ­tica publicitaria;
- no solicita ubicaciÃ³n del telÃ©fono;
- consulta ubicaciones de embarcaciones/dispositivos desde el backend;
- almacena localmente sesiÃ³n cifrada, alertas/cachÃ© operativo y cachÃ© de tiles de mapa.

## Datos recolectados o tratados

### InformaciÃ³n personal

| Tipo Google Play | Â¿Se recopila? | Uso |
|---|---:|---|
| Nombre | SÃ­ | IdentificaciÃ³n del usuario/cliente logoneado |
| DirecciÃ³n de correo electrÃ³nico | SÃ­ | Contacto y datos de perfil |
| NÃºmero de telÃ©fono | SÃ­ | Contacto y datos de perfil |
| Identificadores de usuario | SÃ­ | Login, autorizaciÃ³n y consulta de flota |
| DirecciÃ³n fÃ­sica | No detectado en app | No se solicita en Android |

### Identificadores

| Tipo Google Play | Â¿Se recopila? | Uso |
|---|---:|---|
| ID de usuario | SÃ­ | AutenticaciÃ³n y autorizaciÃ³n |
| ID de dispositivo | SÃ­ | Se envÃ­a `ANDROID_ID` durante login como `device_id` |
| ID de publicidad | No | No hay SDK publicitario |

### UbicaciÃ³n

| Tipo Google Play | Â¿Se recopila? | Uso |
|---|---:|---|
| UbicaciÃ³n precisa del telÃ©fono | No | La app no solicita permisos de ubicaciÃ³n |
| UbicaciÃ³n aproximada del telÃ©fono | No | La app no solicita permisos de ubicaciÃ³n |
| UbicaciÃ³n de activos/embarcaciones | SÃ­, como dato operativo desde servidor | VisualizaciÃ³n de flota, histÃ³rico y alertas |

Nota: en Play Console, explicar que las coordenadas corresponden a embarcaciones/dispositivos marÃ­timos asignados al usuario, no a la ubicaciÃ³n GPS del telÃ©fono.

### Actividad de la app

| Tipo Google Play | Â¿Se recopila? | Uso |
|---|---:|---|
| Interacciones en la app | Limitado/operativo | SelecciÃ³n de cliente, consulta de dispositivos, marcado de alertas |
| Historial de bÃºsqueda en app | No persistido como dato de usuario | BÃºsquedas locales temporales en listas |

### InformaciÃ³n y rendimiento de la app

| Tipo Google Play | Â¿Se recopila? | Uso |
|---|---:|---|
| Registros de fallos | No implementado como SDK externo | No hay Crashlytics/Sentry |
| DiagnÃ³stico | Logs locales de desarrollo | No deberÃ­an enviarse a terceros |

## ComparticiÃ³n de datos

DeclaraciÃ³n sugerida:

- No se comparten datos con terceros comerciales.
- Los datos se transmiten a servidores Nettel/Spot1/Spot2 para prestar el servicio.
- Los mapas usan fuentes OpenStreetMap/OpenSeaMap/osmdroid y pueden descargar tiles de proveedores de mapas. Revisar proveedor final de tiles configurado antes de publicar.

## Seguridad

Declarar:

- Los datos se cifran en trÃ¡nsito mediante HTTPS.
- La sesiÃ³n se almacena localmente usando almacenamiento cifrado Android.
- La app permite solicitar rectificaciÃ³n/eliminaciÃ³n/portabilidad mediante contacto de datos personales.

## EliminaciÃ³n de datos / cuenta

La app actualmente muestra informaciÃ³n de contacto para ejercer derechos de datos:

```text
usodedatos@nettelcorp.com
```

Para Google Play, si aplica cuenta de usuario, se debe proveer un mÃ©todo claro para solicitar eliminaciÃ³n de cuenta/datos. Puede ser:

- URL web pÃºblica de eliminaciÃ³n;
- correo/proceso documentado en polÃ­tica de privacidad;
- opciÃ³n dentro de la app en futuras versiones.

## Respuestas sugeridas en Play Console

- Â¿La app recopila o comparte datos de usuario? SÃ­.
- Â¿Todos los datos se cifran en trÃ¡nsito? SÃ­.
- Â¿Los usuarios pueden solicitar eliminaciÃ³n de datos? SÃ­, si se publica proceso/URL/correo en polÃ­tica de privacidad.
- Â¿La app contiene anuncios? No.
- Â¿Usa permisos de ubicaciÃ³n del dispositivo? No.
- Â¿Usa notificaciones? SÃ­, para alertas operativas.

## Advertencias antes de envÃ­o

1. Confirmar proveedor final de tiles de mapa y su polÃ­tica.
2. Publicar polÃ­tica de privacidad por HTTPS.
3. Alinear exactamente esta declaraciÃ³n con la polÃ­tica de privacidad.
4. Evitar capturas con datos personales reales.
5. Proveer cuenta demo a Google Play.

