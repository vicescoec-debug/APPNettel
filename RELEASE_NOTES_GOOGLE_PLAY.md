# Notas de versiÃ³n â€” Nettel MarÃ­timo

**VersiÃ³n:** `1.0`  
**Version Code:** `51`  
**Fecha:** 3 de agosto de 2026

## Resumen para Google Play

- Ajuste de la pantalla `ConfiguraciÃ³n > Acerca de` para publicaciÃ³n: presentaciÃ³n limpia, sin texto tÃ©cnico interno, con enlace clicable a tÃ©rminos y condiciones.

Nueva versiÃ³n de Nettel MarÃ­timo con mejoras de estabilidad, visualizaciÃ³n de flota, alertas, histÃ³rico y manejo de sesiÃ³n.

## Cambios principales

- ModernizaciÃ³n REST/JSON sobre HTTPS.
- Mapa de flota con OpenSeaMap/osmdroid.
- Clustering de dispositivos optimizado.
- Vista satelital, batimetrÃ­a opcional y capa de pesca marÃ­tima.
- HistÃ³rico con lÃ­nea de ruta, flechas temporales e iconos amarillos.
- Alertas con cachÃ© por usuario/flota.
- Filtro de mensajes informativos SPOT Trace.
- Alertas crÃ­ticas SOS/PT1 con icono rojo Ferrari y parpadeo.
- Alerta local de baterÃ­a baja.
- Perfil de usuario logoneado con correo y celular.
- CorrecciÃ³n de textos ilegibles y mensajes de error tÃ©cnicos.
- RecuperaciÃ³n de contraseÃ±a con validaciÃ³n de campos.
- Mejoras visuales en login, dispositivos, alertas y usuarios.

## Texto corto sugerido para Play Console

Mejoras en mapa de flota, alertas crÃ­ticas SOS, histÃ³rico, perfil de usuario y estabilidad general.

## Texto extendido sugerido

Esta versiÃ³n mejora la experiencia de monitoreo marÃ­timo con mapa OpenSeaMap, agrupaciÃ³n inteligente de dispositivos, alertas crÃ­ticas SOS/PT1 resaltadas en rojo, histÃ³rico con ruta y direcciÃ³n temporal, validaciÃ³n de recuperaciÃ³n de contraseÃ±a, perfil de usuario y mejor manejo de errores.

## Observaciones

Para producciÃ³n se requiere que el backend mantenga disponibles los endpoints REST documentados, especialmente:

- `POST /auth/login`
- `POST /devices`
- `POST /devices/history`
- `POST /alerts`
- `POST /alerts/read`
- `POST /users`
- `POST /users/update` si se habilitarÃ¡ ediciÃ³n de correo/celular.

