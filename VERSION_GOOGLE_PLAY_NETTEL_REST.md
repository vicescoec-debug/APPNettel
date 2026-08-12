# VersiÃ³n candidata para Google Play â€” Nettel MarÃ­timo

**Fecha de actualizaciÃ³n:** 30 de julio de 2026  
**VersiÃ³n tÃ©cnica actual:** `1.0-release`  
**Version Code:** `46`  
**APK debug actual:** `Nettel Maritimo v1.0-release.apk`  
**Package base:** `com.nettel.maritimo.next`  
**Package debug instalado:** `com.nettel.maritimo.next.debug`  
**Min SDK:** Android API 26  
**Target SDK:** Android API 35  
**Arquitectura:** REST + JSON sobre HTTPS

## APK actual para pruebas

```text
C:\Users\espin\Documents\APP Nettel\Nettel Maritimo v1.0-release.apk
```

El APK debug `v1.0` fue compilado e instalado correctamente en el telÃ©fono conectado por ADB.

Para Google Play no se debe subir este APK debug. Se debe generar un Android App Bundle `.aab` firmado en modo release.

## Proyecto y fuentes

Proyecto activo:

```text
C:\Users\espin\Documents\APP Nettel\NettelMaritimoNext
```

Fuentes actualizadas:

```text
C:\Users\espin\Documents\APP Nettel\Fuentes_Actualizados_NettelMaritimoNext
```

APK anteriores:

```text
C:\Users\espin\Documents\APP Nettel\APK_Anteriores
```

## ComunicaciÃ³n backend

La app trabaja principalmente contra:

```text
https://spot1.nettelcorp.com/api/v1/
```

Endpoints requeridos:

- `POST /auth/login`
- `POST /auth/logout`
- `POST /auth/role`
- `POST /auth/password/verify-temporary`
- `POST /auth/password/forgot`
- `POST /auth/password/change`
- `POST /users`
- `POST /devices`
- `POST /devices/history`
- `POST /alerts`
- `POST /alerts/read`

La comunicaciÃ³n esperada es REST + JSON sobre HTTPS.

### Enriquecimiento temporal legacy

Mientras Spot1/Spot2 no entregue toda la telemetrÃ­a por REST, la app usa temporalmente:

```text
https://spot2.nettelcorp.com/gps/Mapa.2.0.Dispositivos.php
```

Campos usados:

- `rumbo`
- `last_rumbo`
- `last_bateria`
- `last_bateria_level`
- `last_energia`
- `tipo_posicion`
- `estado_posicion`
- `tiene_alarma`
- `alerta_robo`

## Funcionalidades incluidas

- Pantalla inicial moderna con logo Nettel.
- Pantalla de bienvenida:

```text
Bienvenido
Control de Flota y Alertas
```

- Login con usuario/contraseÃ±a.
- Ojo para mostrar/ocultar contraseÃ±a.
- RecuperaciÃ³n y cambio de contraseÃ±a.
- Aviso de ProtecciÃ³n de Datos en login.
- Correo de datos personales:

```text
usodedatos@nettelcorp.com
```

- Consulta de usuarios/clientes.
- SelecciÃ³n de cliente/flota activo.
- Consulta de dispositivos asignados.
- Consulta de histÃ³rico.
- Consulta, notificaciÃ³n y marcado de alertas.
- Mapa de flota con OpenStreetMap/OpenSeaMap mediante `osmdroid`.
- Capa opcional de batimetrÃ­a.
- Capa de pesca marÃ­tima en amarillo.
- Vista satelital como mapa base.
- Almacenamiento cifrado de sesiÃ³n.
- WorkManager para revisiÃ³n periÃ³dica de alertas.

## Dispositivos

El mÃ³dulo Dispositivos muestra:

- contador de dispositivos;
- bÃºsqueda por embarcaciÃ³n, matrÃ­cula o serial;
- fecha/hora convertida a UTC-5;
- serial;
- embarcaciÃ³n;
- matrÃ­cula;
- estado de baterÃ­a/carga;
- latitud;
- longitud;
- rumbo;
- velocidad;
- tipo de mensaje.

Al tocar una ficha se abre el mapa centrado en ese dispositivo.

## Mapa de Flota

CaracterÃ­sticas:

- OpenSeaMap/osmdroid.
- Marcador personalizado de embarcaciÃ³n.
- Nombre visible junto al icono.
- Auto-centrado de dispositivos.
- Clustering por pÃ­xeles en pantalla.
- DesagrupaciÃ³n progresiva al hacer zoom.
- Coordenadas en globo limitadas a 4 decimales.
- Hora visual convertida a Ecuador UTC-5.
- BatimetrÃ­a desactivada por defecto.
- Pesca en amarillo desde `assets/zonas-pesca.tsv`.
- Zonas terrestres interiores filtradas.
- RÃ³tulos de zonas de pesca en recuadro.
- SatÃ©lite aplicado como mapa base para disminuir parpadeo.

VisualizaciÃ³n por tipo de alerta:

- Normal: icono de embarcaciÃ³n azul.
- BaterÃ­a baja: icono naranja con pulso/parpadeo naranja.
- SOS/PT1 crÃ­tico: icono rojo Ferrari `#FF2800` con pulso/parpadeo rojo.

Los dispositivos con alerta crÃ­tica SOS/PT1 se dibujan individualmente para no quedar ocultos dentro de clÃºsteres.

En el globo del mapa, cuando el backend envÃ­a `PT1 A`, `PT1 W`, `PT1 P`, `TP1 A/W/P` o `SOS A/W/P`, se muestra:

```text
Tipo: SOS
```

## HistÃ³rico

- Selector con bÃºsqueda.
- Campo inicial en blanco.
- BotÃ³n `X` para limpiar.
- Calendario para selecciÃ³n de fecha.
- Consulta por dispositivo asignado.
- VisualizaciÃ³n en mapa.
- Iconos tipo gota amarilla.
- LÃ­nea amarilla entre puntos.
- Flechas de direcciÃ³n temporal.
- Enriquecimiento de histÃ³rico con datos actuales del dispositivo cuando el backend no entrega rumbo/baterÃ­a/carga.

## Alertas

Incluye:

- consulta REST;
- cachÃ© local por usuario/flota activa;
- separaciÃ³n de alertas leÃ­das por usuario/flota;
- eliminaciÃ³n automÃ¡tica del cachÃ© global antiguo;
- botÃ³n `Marcar todas como leÃ­das`;
- limpieza inmediata del listado al marcar como leÃ­das;
- navegaciÃ³n al mapa al tocar una alerta;
- coordenadas a 4 decimales.

Mensajes informativos filtrados, no considerados alertas:

- `SPOT Trace is functioning properly`
- `SPT Trace is functioning propoerly`
- `SPOT Trace has detected that the asset has moved`

Alertas locales:

- BaterÃ­a baja cuando `last_bateria_level` o voltaje detectado es menor o igual a `3.7 V`.
- SOS/PT1 crÃ­tico para variantes `A`, `W` o `P`.

## Recursos visuales Play Store

Carpeta de imÃ¡genes:

```text
C:\Users\espin\Documents\APP Nettel\PlayStore_Imagenes
```

Icono Play Store:

```text
NettelMaritimoNext\app\src\main\res\drawable-nodpi\play_store_icon_512.png
```

## ValidaciÃ³n realizada

- `assembleDebug --stacktrace`: exitoso.
- InstalaciÃ³n por ADB: exitosa.
- App abierta en telÃ©fono: exitosa.
- Ãšltima versiÃ³n instalada verificada:

```text
versionCode=100
versionName=1.0
```

## Pendientes para Google Play

1. Crear keystore release definitivo.
2. Configurar firma release fuera del repositorio.
3. Generar `.aab` release.
4. Publicar polÃ­tica de privacidad en URL HTTPS.
5. Completar formulario de Seguridad de Datos.
6. Probar en canal interno de Google Play.
7. Validar con usuarios/flotas reales.
8. Migrar el enriquecimiento legacy temporal a REST/JSON completo.

## Comando recomendado para generar release AAB

DespuÃ©s de configurar firma release:

```powershell
$env:JAVA_HOME='C:\Users\espin\Documents\APP Nettel\.tools\jdk17\jdk-17.0.19+10'
$env:ANDROID_HOME='C:\Users\espin\Documents\APP Nettel\.tools\android-sdk'
.\gradlew.bat bundleRelease
```

Salida esperada:

```text
app\build\outputs\bundle\release\app-release.aab
```

## Estado final

La app estÃ¡ lista para continuar pruebas internas y preparaciÃ³n de release. La publicaciÃ³n en Google Play depende de generar el `.aab` firmado y completar los requisitos de consola.




## Firma release generada

Se generó un Android App Bundle firmado para publicación:

`C:\Users\espin\Documents\APP Nettel\Nettel Maritimo v1.0-release-signed.aab`

La keystore/upload key queda archivada únicamente de forma local en:

`C:\Users\espin\Documents\APP Nettel\.signing\APPNettel`

Certificado SHA-256:

`79:B7:6A:0E:D9:3A:23:3E:C9:8B:BB:FF:80:8F:53:3C:1E:AD:1D:1D:45:FF:78:8E:1C:78:44:62:65:D2:07:B1`

Ver detalle en `FIRMA_PUBLICACION_GOOGLE_PLAY.md`.
