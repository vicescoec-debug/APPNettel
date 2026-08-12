# ModularizaciÃ³n actual de Nettel MarÃ­timo

**Fecha de actualizaciÃ³n:** 30 de julio de 2026  
**VersiÃ³n documentada:** `Nettel Maritimo v1.0-release.apk`  
**Package debug:** `com.nettel.maritimo.next.debug`  
**API activa:** `https://spot1.nettelcorp.com/api/v1/`

## Resumen ejecutivo

La aplicaciÃ³n fue reconstruida como cliente Android REST + JSON sobre HTTPS. El uso operativo de SOAP/ASMX y Google Maps fue reemplazado por servicios REST, `osmdroid`, OpenStreetMap/OpenSeaMap, capas marÃ­timas, histÃ³rico, alertas, dispositivos, usuarios, sesiÃ³n segura y notificaciones periÃ³dicas.

La app sigue compilÃ¡ndose como un Ãºnico mÃ³dulo Gradle llamado `app`, pero el cÃ³digo estÃ¡ separado por paquetes funcionales. Esta organizaciÃ³n permite avanzar luego hacia mÃ³dulos Gradle reales sin rediseÃ±ar toda la app.

## APK y fuentes actuales

APK vigente:

```text
C:\Users\espin\Documents\APP Nettel\Nettel Maritimo v1.0-release.apk
```

Fuentes:

```text
C:\Users\espin\Documents\APP Nettel\NettelMaritimoNext
C:\Users\espin\Documents\APP Nettel\Fuentes_Actualizados_NettelMaritimoNext
```

APK anteriores:

```text
C:\Users\espin\Documents\APP Nettel\APK_Anteriores
```

## Estado funcional actual

- Nombre visible: `Nettel Maritimo`.
- `versionName`: `1.0-release`.
- `versionCode`: `51`.
- `minSdk`: 26.
- `targetSdk`: 35.
- ComunicaciÃ³n: REST + JSON + HTTPS.
- Base REST: `https://spot1.nettelcorp.com/api/v1/`.
- Mapa: OpenStreetMap/OpenSeaMap con `osmdroid`.
- Sin dependencia activa de Google Maps.
- Hora visual convertida de UTC a Ecuador UTC-5 en:
  - Dispositivos;
  - Mapa de Flota;
  - HistÃ³rico;
  - Alertas.
- Coordenadas en globos/listados relevantes limitadas a 4 decimales.
- Alertas con cachÃ© separado por usuario/flota activa.
- Alertas leÃ­das ocultas localmente.
- Clustering de flota sensible al zoom.
- Mapa de Flota inicia sin batimetrÃ­a.
- Controles: `BatimetrÃ­a`, `Pesca`, `SatÃ©lite/Mapa`.
- Pesca en amarillo desde `assets/zonas-pesca.tsv`.
- Zonas interiores terrestres filtradas.
- RÃ³tulos de zonas de pesca en recuadro.
- SatÃ©lite usado como mapa base para evitar parpadeo.
- Mensajes/Toast elevados para no quedar tapados por teclado.

## Estructura actual

```text
NettelMaritimoNext/
â””â”€â”€ app/
    â””â”€â”€ src/main/
        â”œâ”€â”€ java/com/nettel/maritimo/next/
        â”‚   â”œâ”€â”€ NettelApp.java
        â”‚   â”œâ”€â”€ data/
        â”‚   â”œâ”€â”€ ui/
        â”‚   â”œâ”€â”€ util/
        â”‚   â””â”€â”€ work/
        â”œâ”€â”€ assets/
        â””â”€â”€ res/
            â”œâ”€â”€ drawable/
            â”œâ”€â”€ drawable-nodpi/
            â”œâ”€â”€ mipmap*/
            â””â”€â”€ values/
```

## Paquete `data`

Responsable de modelos, repositorio, REST, sesiÃ³n segura y persistencia local.

```text
data/
â”œâ”€â”€ Alert.java
â”œâ”€â”€ AlertCache.java
â”œâ”€â”€ ApiResult.java
â”œâ”€â”€ Device.java
â”œâ”€â”€ NettelRepository.java
â”œâ”€â”€ RestClient.java
â”œâ”€â”€ SessionStore.java
â””â”€â”€ User.java
```

Responsabilidades:

- Login/logout.
- Consulta de rol.
- Consulta de usuarios.
- Consulta de dispositivos.
- Consulta de histÃ³rico.
- Consulta y marcado de alertas.
- Manejo de token.
- Persistencia segura de sesiÃ³n.
- Persistencia de cliente/flota activo.
- CachÃ© local de alertas por usuario/flota.
- Lista local de alertas leÃ­das por usuario/flota.
- Mapeo flexible de campos JSON.
- Enriquecimiento temporal desde legacy Spot2.

### `Device.java`

Centraliza el modelo de dispositivo:

- serie;
- nombre;
- matrÃ­cula;
- estado;
- fecha/hora;
- latitud/longitud;
- velocidad;
- rumbo;
- tipo de mensaje;
- estado de carga;
- estado/voltaje de baterÃ­a.

Reglas principales:

- Interpreta mÃºltiples nombres de campos del backend.
- Convierte `PT1 A/W/P`, `TP1 A/W/P` y `SOS A/W/P` en evento crÃ­tico.
- Para presentaciÃ³n en mapa muestra estos eventos como:

```text
Tipo: SOS
```

- Detecta baterÃ­a baja cuando el voltaje es menor o igual a `3.7 V`.

### `Alert.java`

Modelo de alerta:

- tipo;
- mensaje;
- fecha;
- embarcaciÃ³n;
- matrÃ­cula;
- cliente;
- serie;
- latitud/longitud.

TambiÃ©n filtra mensajes informativos SPOT Trace que no deben considerarse alertas:

- `SPOT Trace is functioning properly`
- `SPT Trace is functioning propoerly`
- `SPOT Trace has detected that the asset has moved`

### `AlertCache.java`

Persistencia local de alertas:

- cachÃ© de alertas por usuario/flota activa;
- IDs leÃ­dos por usuario/flota activa;
- limpieza automÃ¡tica del cachÃ© global antiguo;
- filtro de mensajes informativos;
- soporte para alertas locales con IDs negativos.

Esto evita que un usuario vea alertas de otro usuario al cambiar de sesiÃ³n o flota.

### `NettelRepository.java`

Capa de orquestaciÃ³n de datos:

- consume REST Spot1;
- valida respuestas;
- aplica enriquecimiento temporal legacy;
- genera alertas locales:
  - baterÃ­a baja;
  - SOS/PT1 crÃ­tico;
- marca alertas leÃ­das en servidor para IDs positivos;
- mantiene locales las alertas generadas por la app.

## Paquete `ui`

Responsable de pantallas, navegaciÃ³n y componentes visuales.

```text
ui/
â”œâ”€â”€ AlertsActivity.java
â”œâ”€â”€ BaseActivity.java
â”œâ”€â”€ DevicesActivity.java
â”œâ”€â”€ HistoryActivity.java
â”œâ”€â”€ LoginActivity.java
â”œâ”€â”€ MainActivity.java
â”œâ”€â”€ MapActivity.java
â”œâ”€â”€ PasswordChangeActivity.java
â”œâ”€â”€ RecoveryActivity.java
â”œâ”€â”€ SettingsActivity.java
â”œâ”€â”€ SplashActivity.java
â”œâ”€â”€ Ui.java
â””â”€â”€ UsersActivity.java
```

### `AlertsActivity.java`

MÃ³dulo Alertas:

- muestra alertas REST y locales;
- oculta alertas leÃ­das;
- permite marcar todas como leÃ­das;
- al marcar, limpia inmediatamente el listado visible;
- al tocar una alerta abre el mapa centrado en el dispositivo/ubicaciÃ³n;
- coordenadas a 4 decimales.

### `MapActivity.java`

MÃ³dulo de mapa:

- OpenSeaMap/osmdroid;
- control de batimetrÃ­a;
- control de pesca;
- control satÃ©lite/mapa;
- clusters de flota;
- nombres de embarcaciones;
- globos con datos operativos;
- histÃ³rico con ruta y flechas;
- iconos/pulsos por alerta.

Prioridad visual:

1. SOS/PT1 crÃ­tico: rojo Ferrari `#FF2800` y parpadeo rojo.
2. BaterÃ­a baja: naranja y parpadeo naranja.
3. Normal: icono estÃ¡ndar azul.

Los crÃ­ticos se dibujan individualmente para no quedar ocultos en clÃºsteres.

### `DevicesActivity.java`

MÃ³dulo Dispositivos:

- listado ordenado del mÃ¡s reciente al mÃ¡s antiguo;
- buscador por embarcaciÃ³n, matrÃ­cula o serial;
- contador visible;
- ficha completa;
- click abre mapa.

### `HistoryActivity.java`

MÃ³dulo HistÃ³rico:

- selector buscable;
- campo inicial en blanco;
- botÃ³n `X`;
- calendario;
- consulta por dispositivo asignado;
- envÃ­o al mapa de serie, nombre y rango de fechas.

## Paquete `work`

```text
work/
â”œâ”€â”€ AlertWorker.java
â””â”€â”€ NotificationHelper.java
```

Responsabilidades:

- revisiÃ³n periÃ³dica de alertas;
- uso de WorkManager;
- notificaciones Android modernas;
- respeto de alertas leÃ­das;
- filtro de mensajes informativos.

## Paquete `util`

```text
util/
â””â”€â”€ DateTimeUtils.java
```

Responsabilidades:

- conversiÃ³n de fechas UTC a hora Ecuador UTC-5;
- parseo flexible para fechas de backend.

## Assets y recursos relevantes

```text
app/src/main/assets/zonas-pesca.tsv
app/src/main/res/drawable-nodpi/splash_cover.png
app/src/main/res/drawable-nodpi/splash_welcome.png
app/src/main/res/drawable-nodpi/play_store_icon_512.png
app/src/main/res/drawable/marker_boat.xml
app/src/main/res/drawable/ic_history_pin_yellow.xml
```

## ComunicaciÃ³n y servicios

### REST principal

```text
https://spot1.nettelcorp.com/api/v1/
```

### Legacy temporal

```text
https://spot2.nettelcorp.com/gps/Mapa.2.0.Dispositivos.php
```

Uso legacy temporal:

- completar rumbo;
- completar baterÃ­a/carga;
- completar tipo de posiciÃ³n;
- completar estado de posiciÃ³n;
- detectar SOS/PT1 crÃ­tico cuando REST principal no lo entregue.

## RecomendaciÃ³n de modularizaciÃ³n futura

Cuando se decida dividir en mÃ³dulos Gradle reales, se recomienda:

```text
:app
:core-data
:core-network
:core-ui
:feature-login
:feature-devices
:feature-map
:feature-history
:feature-alerts
:feature-users
:feature-settings
:background-work
```

Orden sugerido:

1. Extraer `data` a `:core-data` y `:core-network`.
2. Extraer componentes comunes `Ui`, `BaseActivity`, recursos compartidos a `:core-ui`.
3. Separar pantallas por feature.
4. Separar WorkManager/notificaciones en `:background-work`.

## ValidaciÃ³n tÃ©cnica

Ãšltima validaciÃ³n:

- `assembleDebug --stacktrace`: exitoso.
- InstalaciÃ³n ADB: exitosa.
- VersiÃ³n instalada: `1.0-release`.
- `versionCode`: `51`.

## Estado final

La estructura actual ya permite mantenimiento incremental. Para crecimiento futuro, la divisiÃ³n recomendada es separar primero la capa `data/network`, despuÃ©s componentes UI comunes, y finalmente cada feature funcional.


