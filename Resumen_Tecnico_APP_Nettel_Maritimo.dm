# Resumen tÃ©cnico â€” APP Nettel MarÃ­timo

**Fecha de actualizaciÃ³n:** 30 de julio de 2026  
**VersiÃ³n documentada:** `Nettel Maritimo v1.0-release.apk`  
**Package debug:** `com.nettel.maritimo.next.debug`  
**API activa:** `https://spot1.nettelcorp.com/api/v1/`  
**Arquitectura:** Android Java + REST/JSON + HTTPS + osmdroid/OpenSeaMap

## Resultado ejecutivo

La aplicaciÃ³n Nettel MarÃ­timo fue reconstruida y modernizada como una app Android funcional para monitoreo de flota marÃ­tima. La versiÃ³n actual reemplaza la dependencia operativa de SOAP/ASMX y Google Maps por servicios REST/JSON sobre HTTPS y mapas OpenStreetMap/OpenSeaMap mediante `osmdroid`.

La Ãºltima APK generada e instalada para pruebas es:

```text
C:\Users\espin\Documents\APP Nettel\Nettel Maritimo v1.0-release.apk
```

Estado actual:

- CompilaciÃ³n debug correcta.
- InstalaciÃ³n en telÃ©fono conectada por ADB correcta.
- VersiÃ³n instalada: `versionCode=100`, `versionName=1.0`.
- Fuentes activas en:

```text
C:\Users\espin\Documents\APP Nettel\NettelMaritimoNext
C:\Users\espin\Documents\APP Nettel\Fuentes_Actualizados_NettelMaritimoNext
```

## Funcionalidades principales implementadas

### AutenticaciÃ³n y sesiÃ³n

- Login REST contra Spot1.
- Logout local/remoto.
- RecuperaciÃ³n y cambio de contraseÃ±a.
- Almacenamiento seguro de sesiÃ³n con `EncryptedSharedPreferences`.
- Selector de cliente/flota activo.
- Regla temporal conservada: si el usuario es `gfanny` y no hay cliente seleccionado, se usa `rdiego`.

### Pantallas iniciales

- Portada moderna con logo Nettel y diseÃ±o tecnolÃ³gico/satelital.
- Segunda pantalla de bienvenida:

```text
Bienvenido
Control de Flota y Alertas
```

- DuraciÃ³n aproximada de 2 segundos antes del login.
- Eliminada la imagen inicial con fondo blanco.

### Login

- Usuario y contraseÃ±a visibles correctamente.
- OpciÃ³n de ojo para mostrar/ocultar contraseÃ±a.
- Texto de ProtecciÃ³n de Datos centrado bajo los botones.
- Correo actualizado:

```text
usodedatos@nettelcorp.com
```

### Dispositivos

- Lista de dispositivos asignados al usuario/cliente activo.
- Ordenamiento del mÃ¡s reciente al mÃ¡s antiguo.
- Contador de dispositivos enlistados.
- Buscador superior por embarcaciÃ³n, matrÃ­cula o serial.
- VisualizaciÃ³n de:
  - fecha/hora convertida de UTC a Ecuador UTC-5;
  - serial;
  - embarcaciÃ³n;
  - matrÃ­cula;
  - estado de baterÃ­a/carga;
  - latitud;
  - longitud;
  - rumbo;
  - velocidad;
  - tipo de mensaje o estado.
- Click sobre un dispositivo abre el mapa centrado en ese dispositivo.
- Textos corregidos sin caracteres daÃ±ados.

### Mapa de Flota

- Mapa basado en OpenStreetMap/OpenSeaMap con `osmdroid`.
- Sin dependencia activa de Google Maps.
- Auto-centrado segÃºn dispositivos visibles.
- Icono personalizado de embarcaciÃ³n.
- Nombre visible junto al icono.
- Coordenadas en globos con 4 decimales.
- Hora convertida a UTC-5.
- Clustering por distancia visual en pantalla:
  - agrupa en zoom bajo/medio;
  - se desagrupa progresivamente;
  - desde zoom alto permite ver dispositivos separados.
- Cache de iconos de cluster para reducir consumo.
- Etiquetas condicionadas por zoom/cantidad para evitar exceso de overlays.

Controles disponibles:

- `BatimetrÃ­a`: desactivada al ingresar.
- `Pesca`: muestra zonas marÃ­timas en amarillo desde `assets/zonas-pesca.tsv`.
- `SatÃ©lite/Mapa`: alterna vista satelital como mapa base para reducir parpadeo.

Mejoras de capa de pesca:

- Filtro para no mostrar zonas interiores terrestres.
- PolÃ­gonos marÃ­timos en amarillo.
- Nombre de cada zona en recuadro junto a la zona.

### HistÃ³rico de Dispositivos

- Selector de dispositivo con bÃºsqueda.
- Campo inicial en blanco; no se autorellena hasta que el usuario selecciona.
- BotÃ³n `X` para limpiar bÃºsqueda.
- Selector de fecha con calendario.
- Consulta histÃ³rica por dispositivo asignado.
- Resultado en mapa.
- Iconos histÃ³ricos tipo gota amarilla.
- LÃ­nea amarilla uniendo puntos.
- Flechas de direcciÃ³n temporal desde la ubicaciÃ³n mÃ¡s temprana hacia la mÃ¡s reciente.
- Enriquecimiento de histÃ³rico con datos del dispositivo cuando el backend no devuelve:
  - rumbo;
  - estado de baterÃ­a;
  - voltaje/carga;
  - nombre;
  - matrÃ­cula;
  - serial;
  - tipo.

### Alertas

- Consulta de alertas por REST.
- Notificaciones periÃ³dicas mediante WorkManager.
- SincronizaciÃ³n al abrir la app.
- Marcado de alertas como leÃ­das.
- Las alertas leÃ­das se ocultan localmente y no reaparecen al actualizar si el servidor vuelve a retornarlas.
- Al tocar una alerta, se abre el mapa y se centra en el dispositivo alertado cuando hay coordenadas/serie disponible.
- Latitud y longitud en listado de alertas a 4 decimales.
- CachÃ© de alertas separado por usuario/flota activa para evitar mezcla de datos entre usuarios.
- CachÃ© global antiguo eliminado automÃ¡ticamente al inicializar el mÃ³dulo.
- Al presionar `Marcar todas como leÃ­das`, el listado desaparece inmediatamente.

Filtros de mensajes informativos:

Ya no se consideran alertas los mensajes:

- `SPOT Trace is functioning properly`
- `SPT Trace is functioning propoerly`
- `SPOT Trace has detected that the asset has moved`

Alertas locales generadas por la app:

- BaterÃ­a baja cuando el voltaje detectado es menor o igual a `3.7 V`.
- SOS/PT1 crÃ­tico para variantes:
  - `SOS A`
  - `SOS W`
  - `SOS P`
  - `PT1 A`
  - `PT1 W`
  - `PT1 P`
  - `TP1 A/W/P` como compatibilidad.

RepresentaciÃ³n visual en mapa:

- BaterÃ­a baja: icono naranja con pulso/parpadeo naranja.
- SOS/PT1 crÃ­tico: icono rojo Ferrari `#FF2800` con pulso/parpadeo rojo.
- Los dispositivos con alerta crÃ­tica no quedan ocultos dentro del clÃºster; se dibujan individualmente.
- En el globo del mapa, aunque el backend envÃ­e `PT1 A`, `PT1 W` o `PT1 P`, se muestra:

```text
Tipo: SOS
```

## IntegraciÃ³n de datos

### REST principal

Base activa:

```text
https://spot1.nettelcorp.com/api/v1/
```

Endpoints usados:

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

### Enriquecimiento legacy temporal

Para completar rumbo, baterÃ­a, carga, tipo de posiciÃ³n y estado de posiciÃ³n, la app consulta temporalmente:

```text
https://spot2.nettelcorp.com/gps/Mapa.2.0.Dispositivos.php
```

Campos usados para enriquecer dispositivos:

- `rumbo`
- `last_rumbo`
- `last_bateria`
- `last_bateria_level`
- `last_energia`
- `tipo_posicion`
- `estado_posicion`
- `tiene_alarma`
- `alerta_robo`

## OptimizaciÃ³n de mapa/osmdroid

Cambios incorporados:

- BatimetrÃ­a desactivada por defecto para reducir carga inicial.
- Vista satelital aplicada como mapa base, no como overlay.
- Cache de tiles ampliado.
- Mayor cola/hilos de lectura y descarga de tiles.
- Zoom redondeado a niveles enteros.
- Escalado de tiles a DPI.
- Clustering por pÃ­xeles, no por distancia fija geogrÃ¡fica.
- Menos overlays visibles en zoom bajo.
- ReutilizaciÃ³n/cache de iconos.
- Etiquetas condicionadas por zoom.

## Estado de publicaciÃ³n Google Play

La app estÃ¡ en estado funcional para pruebas internas. Para Google Play todavÃ­a se debe generar una versiÃ³n release/AAB firmada.

Pendientes antes de producciÃ³n:

1. Crear keystore release definitivo.
2. Generar Android App Bundle `.aab`.
3. Probar en canal interno de Google Play.
4. Publicar polÃ­tica de privacidad en URL HTTPS.
5. Completar formulario de Seguridad de Datos en Play Console.
6. Validar comportamiento con usuarios reales y flotas grandes.
7. Migrar completamente el enriquecimiento legacy temporal hacia REST/JSON en Spot1/Spot2.

## Comando de compilaciÃ³n debug usado

```powershell
$env:JAVA_HOME='C:\Users\espin\Documents\APP Nettel\.tools\jdk17\jdk-17.0.19+10'
$env:ANDROID_HOME='C:\Users\espin\Documents\APP Nettel\.tools\android-sdk'
.\gradlew.bat --no-daemon assembleDebug --stacktrace
```

## ConclusiÃ³n

La aplicaciÃ³n Nettel MarÃ­timo ya cuenta con una base moderna y operativa para Android actual: REST/JSON, mapas OpenSeaMap/osmdroid, alertas, histÃ³rico, dispositivos, usuarios, sesiÃ³n segura y visualizaciÃ³n marÃ­tima.

El foco siguiente debe ser estabilizar el backend REST para que toda la informaciÃ³n enriquecida llegue directamente desde Spot1/Spot2 sin depender del endpoint legacy temporal.



